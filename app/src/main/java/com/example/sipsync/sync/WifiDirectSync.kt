package com.example.sipsync.sync

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.WpsInfo
import android.net.wifi.p2p.WifiP2pConfig
import android.net.wifi.p2p.WifiP2pDevice
import android.net.wifi.p2p.WifiP2pInfo
import android.net.wifi.p2p.WifiP2pManager
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest
import android.os.Build
import android.os.Looper
import com.example.sipsync.data.Repository
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Real Wi-Fi Direct sync between the Owner phone and the Employee phone.
 *
 *  - Both phones open the Sync screen: each advertises a "sipsync" service.
 *  - One phone taps "Tap to Sync": it finds the other phone's service and connects.
 *    (The other phone gets Android's own "accept connection" prompt.)
 *  - The phone that becomes group owner runs a TCP server, the other connects to it.
 *  - They swap data, and each marks what it sent as synced once the other confirms.
 *
 * Permissions must be granted before start() is called (see requiredPermissions()).
 */
@SuppressLint("MissingPermission")
class WifiDirectSync(private val app: Application, private val repo: Repository) {

    companion object {
        private const val SERVICE_NAME = "sipsync"
        private const val SERVICE_TYPE = "_sipsync._tcp"
        private const val PORT = 8988
        private const val MAGIC = "SIPSYNC1"
        private const val MAX_FRAME = 20 * 1024 * 1024
        private const val SEARCH_TIMEOUT_MS = 40_000L
        private const val CONNECT_TIMEOUT_MS = 45_000L
        private const val COOLDOWN_MS = 6_000L
        private const val AUTO_CHECK_MS = 15_000L   // how often the Employee phone looks for work
        private const val AUTO_MAX_FAILURES = 5     // stop retrying by itself after this many fails

        fun requiredPermissions(): Array<String> =
            if (Build.VERSION.SDK_INT >= 33) arrayOf(Manifest.permission.NEARBY_WIFI_DEVICES)
            else arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private val _status = MutableStateFlow<SyncStatus>(SyncStatus.Idle)
    val status: StateFlow<SyncStatus> = _status

    private val manager = app.getSystemService(Context.WIFI_P2P_SERVICE) as? WifiP2pManager
    private var channel: WifiP2pManager.Channel? = null
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    @Volatile private var started = false
    @Volatile private var connecting = false
    @Volatile private var syncing = false
    @Volatile private var cooldownUntil = 0L
    @Volatile private var role = "EMPLOYEE"
    @Volatile private var peerName = "Nearby phone"
    private var receiver: BroadcastReceiver? = null
    private var timeoutJob: Job? = null
    private var autoJob: Job? = null
    @Volatile private var autoFailures = 0

    // ---------------------------------------------------------------- lifecycle

    /** Start listening and advertising. Call when the Sync screen opens. */
    fun start(role: String) {
        this.role = role
        if (started) return
        val m = manager
        if (m == null) {
            _status.value = SyncStatus.Failed("This phone does not support Wi-Fi Direct")
            return
        }
        try {
            channel = m.initialize(app, Looper.getMainLooper(), null)
            registerReceiver()
            advertise()
            started = true
            _status.value = SyncStatus.Idle
            startAuto()
        } catch (e: SecurityException) {
            _status.value = SyncStatus.Failed("Allow the Nearby devices permission to sync")
        }
    }

    /** Stop everything. Call when the Sync screen closes. */
    fun stop() {
        if (!started) return
        started = false
        autoJob?.cancel()
        autoJob = null
        timeoutJob?.cancel()
        receiver?.let {
            try { app.unregisterReceiver(it) } catch (_: IllegalArgumentException) {}
        }
        receiver = null
        val m = manager
        val ch = channel
        if (m != null && ch != null) {
            try {
                m.clearLocalServices(ch, noop())
                m.clearServiceRequests(ch, noop())
                m.stopPeerDiscovery(ch, noop())
                m.cancelConnect(ch, noop())
                m.removeGroup(ch, noop())
            } catch (_: Exception) {}
            if (Build.VERSION.SDK_INT >= 27) {
                try { ch.close() } catch (_: Exception) {}
            }
        }
        channel = null
        connecting = false
        syncing = false
        _status.value = SyncStatus.Idle
    }

    /** Stop and release the background scope (call from ViewModel.onCleared). */
    fun close() {
        stop()
        scope.cancel()
    }

    // ---------------------------------------------------------------- discovery

    private fun advertise() {
        val m = manager ?: return
        val ch = channel ?: return
        val info = WifiP2pDnsSdServiceInfo.newInstance(SERVICE_NAME, SERVICE_TYPE, mapOf("role" to role))
        val add = { m.addLocalService(ch, info, noop()) }
        m.clearLocalServices(ch, action(onOk = { add() }, onFail = { add() }))
    }

    /**
     * Automatic sync. Only the Employee phone searches, so two phones never try to
     * connect to each other at the same time. The Owner phone just stays discoverable.
     *
     * The Employee phone syncs by itself:
     *  - once when the Sync screen opens (to get the account, catalog and any changes)
     *  - again whenever it has new transactions waiting to be sent
     * If an attempt fails it retries every few seconds, up to AUTO_MAX_FAILURES times.
     * Tapping the sync button resets that counter.
     */
    private fun startAuto() {
        if (role != "EMPLOYEE") return
        autoJob?.cancel()
        autoFailures = 0
        autoJob = scope.launch {
            var needInitial = true
            var attempted = false
            delay(2_000) // let advertising settle first
            while (isActive && started) {
                val st = _status.value
                val busy = syncing || connecting ||
                    st is SyncStatus.Searching || st is SyncStatus.Connecting || st is SyncStatus.Syncing

                if (!busy) {
                    if (attempted) {
                        // The last automatic attempt has finished: did it work?
                        attempted = false
                        if (st is SyncStatus.Done) {
                            needInitial = false
                            autoFailures = 0
                        } else {
                            autoFailures++
                        }
                    }
                    if (System.currentTimeMillis() >= cooldownUntil && autoFailures < AUTO_MAX_FAILURES) {
                        val pending = try {
                            repo.buildSyncPayload(role).transactions.isNotEmpty()
                        } catch (e: Exception) { false }
                        if (needInitial || pending) {
                            attempted = true
                            withContext(Dispatchers.Main) { startSearch() }
                        }
                    }
                }
                delay(if (busy) 2_000 else AUTO_CHECK_MS)
            }
        }
    }

    /** Tap to Sync: look for the other SipSync phone and connect to it. */
    fun syncNow() {
        autoFailures = 0 // a manual tap always gets a fresh start
        startSearch()
    }

    private fun startSearch() {
        if (!started || syncing) return
        val m = manager ?: return
        val ch = channel ?: return
        connecting = false
        _status.value = SyncStatus.Searching

        m.setDnsSdResponseListeners(
            ch,
            WifiP2pManager.DnsSdServiceResponseListener { instanceName, _, device ->
                if (instanceName.equals(SERVICE_NAME, ignoreCase = true)) connectTo(device)
            },
            WifiP2pManager.DnsSdTxtRecordListener { _, _, _ -> }
        )

        val searchFailed = { code: Int -> fail("Could not search for nearby phones (code $code)") }
        val addRequestAndSearch = {
            m.addServiceRequest(
                ch,
                WifiP2pDnsSdServiceRequest.newInstance(),
                action(
                    onOk = {
                        m.discoverPeers(
                            ch,
                            action(
                                onOk = { m.discoverServices(ch, action(onFail = searchFailed)) },
                                onFail = searchFailed
                            )
                        )
                    },
                    onFail = searchFailed
                )
            )
        }
        m.clearServiceRequests(ch, action(onOk = { addRequestAndSearch() }, onFail = { addRequestAndSearch() }))

        timeoutJob?.cancel()
        timeoutJob = scope.launch {
            delay(SEARCH_TIMEOUT_MS)
            if (_status.value is SyncStatus.Searching) {
                fail("No SipSync phone found. Open the Sync screen on the other phone too.")
            }
        }
    }

    private fun connectTo(device: WifiP2pDevice) {
        if (connecting || syncing) return
        val m = manager ?: return
        val ch = channel ?: return
        connecting = true
        timeoutJob?.cancel()
        peerName = device.deviceName?.takeIf { it.isNotBlank() } ?: "Nearby phone"
        _status.value = SyncStatus.Connecting(peerName)

        val config = WifiP2pConfig().apply {
            deviceAddress = device.deviceAddress
            wps.setup = WpsInfo.PBC
            // The Owner phone prefers to host the group (and the TCP server)
            groupOwnerIntent = if (role == "OWNER") 15 else 0
        }
        m.connect(ch, config, action(onFail = { code -> fail("Could not connect (code $code)") }))

        timeoutJob = scope.launch {
            delay(CONNECT_TIMEOUT_MS)
            if (_status.value is SyncStatus.Connecting) fail("Connection timed out")
        }
    }

    private fun registerReceiver() {
        val filter = IntentFilter().apply {
            addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION)
            addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION)
        }
        val r = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.action) {
                    WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION -> {
                        val state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1)
                        if (state == WifiP2pManager.WIFI_P2P_STATE_DISABLED && !syncing) {
                            _status.value = SyncStatus.Failed("Turn on Wi-Fi to sync")
                        }
                    }
                    WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION -> requestInfo()
                }
            }
        }
        receiver = r
        if (Build.VERSION.SDK_INT >= 33) {
            app.registerReceiver(r, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            app.registerReceiver(r, filter)
        }
    }

    // ---------------------------------------------------------------- connection

    private fun requestInfo() {
        val m = manager ?: return
        val ch = channel ?: return
        m.requestConnectionInfo(ch, WifiP2pManager.ConnectionInfoListener { info -> onConnectionInfo(info) })
    }

    // Runs on both phones once the Wi-Fi Direct group exists
    private fun onConnectionInfo(info: WifiP2pInfo) {
        if (!info.groupFormed || syncing) return
        if (System.currentTimeMillis() < cooldownUntil) return
        syncing = true
        timeoutJob?.cancel()
        _status.value = SyncStatus.Syncing(peerName)

        scope.launch {
            try {
                val (sent, received) = if (info.isGroupOwner) {
                    runServer()
                } else {
                    val host = info.groupOwnerAddress?.hostAddress
                        ?: throw IOException("Could not find the other phone")
                    runClient(host)
                }
                _status.value = SyncStatus.Done(peerName, sent, received)
            } catch (e: Exception) {
                _status.value = SyncStatus.Failed(e.message ?: "Sync failed")
            } finally {
                syncing = false
                connecting = false
                cooldownUntil = System.currentTimeMillis() + COOLDOWN_MS
                withContext(Dispatchers.Main) { leaveGroup() }
            }
        }
    }

    private fun leaveGroup() {
        val m = manager ?: return
        val ch = channel ?: return
        try {
            m.cancelConnect(ch, noop())
            m.removeGroup(ch, noop())
        } catch (_: Exception) {}
    }

    private fun fail(reason: String) {
        timeoutJob?.cancel()
        connecting = false
        _status.value = SyncStatus.Failed(reason)
        leaveGroup()
    }

    // ---------------------------------------------------------------- data exchange
    //
    // Client -> Server : its data
    // Server -> Client : 1 byte (1 = applied, 0 = refused), then its data
    // Client -> Server : 1 byte (1 = applied)
    // Each side marks the transactions it sent as synced only after the other side confirmed.

    private suspend fun runClient(host: String): Pair<Int, Int> {
        val payload = repo.buildSyncPayload(role)

        var socket: Socket? = null
        var attempts = 0
        while (socket == null && attempts < 8) {
            attempts++
            try {
                socket = Socket().apply { connect(InetSocketAddress(host, PORT), 3000) }
            } catch (e: IOException) {
                delay(1000) // the other phone's server may not be ready yet
            }
        }
        val connected = socket
            ?: throw IOException("Could not reach the other phone. Keep its Sync screen open.")

        return connected.use { sock ->
            sock.soTimeout = 30_000
            val out = DataOutputStream(BufferedOutputStream(sock.getOutputStream()))
            val inp = DataInputStream(BufferedInputStream(sock.getInputStream()))

            writeFrame(out, payload)
            if (inp.readByte().toInt() != 1) throw IOException(sameRoleMessage())
            repo.markTransactionsSynced(payload.transactions.map { it.id })

            val theirs = readFrame(inp)
            repo.applySyncPayload(theirs)
            out.writeByte(1)
            out.flush()

            payload.transactions.size to theirs.transactions.size
        }
    }

    private suspend fun runServer(): Pair<Int, Int> {
        val payload = repo.buildSyncPayload(role)

        val server = ServerSocket()
        server.reuseAddress = true
        server.bind(InetSocketAddress(PORT))
        server.soTimeout = 30_000

        return server.use { srv ->
            srv.accept().use { sock ->
                sock.soTimeout = 30_000
                val out = DataOutputStream(BufferedOutputStream(sock.getOutputStream()))
                val inp = DataInputStream(BufferedInputStream(sock.getInputStream()))

                val theirs = readFrame(inp)
                if (theirs.role == role) {
                    out.writeByte(0)
                    out.flush()
                    throw IOException(sameRoleMessage())
                }
                repo.applySyncPayload(theirs)

                out.writeByte(1)
                writeFrame(out, payload)
                if (inp.readByte().toInt() == 1) {
                    repo.markTransactionsSynced(payload.transactions.map { it.id })
                }

                payload.transactions.size to theirs.transactions.size
            }
        }
    }

    private fun sameRoleMessage() =
        "Both phones are logged in as ${role.lowercase()}. Use one Owner and one Employee phone."

    private fun writeFrame(out: DataOutputStream, p: SyncPayload) {
        val bytes = SyncCodec.encode(p).toByteArray(Charsets.UTF_8)
        out.writeUTF(MAGIC)
        out.writeInt(bytes.size)
        out.write(bytes)
        out.flush()
    }

    private fun readFrame(inp: DataInputStream): SyncPayload {
        if (inp.readUTF() != MAGIC) throw IOException("The other device is not running SipSync")
        val size = inp.readInt()
        if (size !in 1..MAX_FRAME) throw IOException("Received invalid data")
        val bytes = ByteArray(size)
        inp.readFully(bytes)
        return SyncCodec.decode(String(bytes, Charsets.UTF_8))
    }

    // ---------------------------------------------------------------- helpers

    private fun action(onOk: () -> Unit = {}, onFail: (Int) -> Unit = {}) =
        object : WifiP2pManager.ActionListener {
            override fun onSuccess() = onOk()
            override fun onFailure(reason: Int) = onFail(reason)
        }

    private fun noop() = action()
}