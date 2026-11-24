package com.example.sipsync.screens

import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sipsync.sync.SyncStatus
import com.example.sipsync.sync.WifiDirectSync

private val SyncGreen = Color(0xFF1E8E5A)
private val SyncGreenBg = Color(0xFFDDF2E8)
private val SyncRed = Color(0xFFD64545)
private val SyncRedBg = Color(0xFFFFE0E6)
private val OwnerTileBg = Color(0xFFFFE0E8)
private val EmployeeTileBg = Color(0xFFFFE3C7)

@Composable
fun SyncScreen(
    isOwner: Boolean,
    status: SyncStatus = SyncStatus.Idle,
    pendingCount: Int = 0,
    readyToSend: List<Txn> = emptyList(),
    onStart: () -> Unit = {},   // start advertising to the other phone
    onSyncNow: () -> Unit = {}, // search + connect + exchange data
    onStop: () -> Unit = {}
) {
    val context = LocalContext.current
    val cardShape = RoundedCornerShape(24.dp)

    var lastSync by remember { mutableStateOf<String?>(null) }
    var lastPeer by remember { mutableStateOf<String?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }

    fun hasPermission() = WifiDirectSync.requiredPermissions().all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            permissionDenied = false
            onStart()
            onSyncNow()
        } else {
            permissionDenied = true
        }
    }

    // Listen for the other phone while this screen is open
    DisposableEffect(Unit) {
        if (hasPermission()) onStart()
        onDispose { onStop() }
    }

    LaunchedEffect(status) {
        when (status) {
            is SyncStatus.Done -> {
                lastSync = "Just now · Sent ${status.sent} · Received ${status.received}"
                lastPeer = status.peer
            }
            is SyncStatus.Connecting -> lastPeer = status.peer
            is SyncStatus.Syncing -> lastPeer = status.peer
            else -> {}
        }
    }

    val busy = status is SyncStatus.Searching || status is SyncStatus.Connecting || status is SyncStatus.Syncing
    val failed = status is SyncStatus.Failed || permissionDenied

    val pillText = when (status) {
        is SyncStatus.Idle -> "Ready"
        is SyncStatus.Searching -> "Searching"
        is SyncStatus.Connecting -> "Connecting"
        is SyncStatus.Syncing -> "Syncing"
        is SyncStatus.Done -> "Synced"
        is SyncStatus.Failed -> "Sync failed"
    }
    val pillColor = when {
        failed -> SyncRed
        busy -> AuthOrange
        else -> SyncGreen
    }
    val subtitle = when {
        permissionDenied -> "Allow the Nearby devices permission to sync"
        status is SyncStatus.Searching -> "Looking for the other SipSync phone…"
        status is SyncStatus.Connecting -> "Accept the connection request on the other phone if asked"
        status is SyncStatus.Syncing -> "Exchanging transactions…"
        status is SyncStatus.Done -> "Sent ${status.sent} · Received ${status.received}"
        status is SyncStatus.Failed -> status.reason
        pendingCount == 0 -> "Everything is synced"
        pendingCount == 1 -> "1 Transaction Ready To Exchange"
        else -> "$pendingCount Transactions Ready To Exchange"
    }

    // The sync icon spins while a sync is running
    val infinite = rememberInfiniteTransition(label = "sync-spin")
    val spinAngle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing)),
        label = "angle"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ===== Header =====
        Text(
            "WI-FI DIRECT", color = AuthRaspberry, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
        )
        Text("Device Sync", color = AuthPlum, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)

        Spacer(Modifier.height(10.dp))

        // Status pill
        val pill = RoundedCornerShape(50)
        Row(
            modifier = Modifier
                .clip(pill)
                .background(Color.White.copy(alpha = 0.85f))
                .border(1.dp, AuthFieldBorder, pill)
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(pillColor))
            Spacer(Modifier.width(8.dp))
            Text(pillText, color = AuthPlum, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(" · No internet required", color = AuthMuted, fontSize = 11.sp)
        }

        Spacer(Modifier.height(16.dp))

        // ===== Sync card: two phones and the big Tap to Sync button =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, cardShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(cardShape)
                .background(Color.White)
                .padding(vertical = 20.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isOwner) {
                    DeviceTile("Owner", "This Device", OwnerTileBg, AuthRaspberry)
                } else {
                    DeviceTile("Employee", "This Device", EmployeeTileBg, AuthOrange)
                }

                // Rings + button
                Box(
                    modifier = Modifier
                        .size(132.dp)
                        .clip(CircleShape)
                        .background(AuthRaspberry.copy(alpha = 0.07f))
                        .border(1.dp, AuthRaspberry.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(112.dp)
                            .clip(CircleShape)
                            .background(AuthRaspberry.copy(alpha = 0.10f))
                            .border(1.dp, AuthRaspberry.copy(alpha = 0.2f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(88.dp)
                                .shadow(12.dp, CircleShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(listOf(AuthRaspberry, AuthOrange)))
                                .clickable(enabled = !busy, role = Role.Button) {
                                    if (hasPermission()) {
                                        permissionDenied = false
                                        onStart()
                                        onSyncNow()
                                    } else {
                                        permissionLauncher.launch(WifiDirectSync.requiredPermissions())
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Outlined.Sync, "Tap to sync", tint = Color.White,
                                modifier = Modifier
                                    .size(40.dp)
                                    .graphicsLayer { rotationZ = if (busy) spinAngle else 0f }
                            )
                        }
                    }
                }

                if (isOwner) {
                    DeviceTile("Employee", "Authorized Peer", EmployeeTileBg, AuthOrange)
                } else {
                    DeviceTile("Owner", "Authorized Peer", OwnerTileBg, AuthRaspberry)
                }
            }

            Spacer(Modifier.height(14.dp))
            Text(
                if (busy) "Please wait…" else "Tap to Sync",
                color = AuthPlum, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold
            )
            Text(
                subtitle,
                color = if (failed) SyncRed else AuthMuted,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(14.dp))

        // ===== Ready to send =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, cardShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(cardShape)
                .background(Color.White)
                .padding(vertical = 14.dp)
        ) {
            Text(
                "READY TO SEND", color = AuthRaspberry, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(Modifier.height(8.dp))
            if (readyToSend.isEmpty()) {
                Text(
                    "Nothing waiting to send",
                    color = AuthMuted, fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
            readyToSend.take(3).forEachIndexed { index, t ->
                if (index > 0) HorizontalDivider(color = AuthFieldBorder)
                ReadyRow(t)
            }
            if (readyToSend.size > 3) {
                Text(
                    "+${readyToSend.size - 3} more",
                    color = AuthMuted, fontSize = 11.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        // ===== Last sync =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, cardShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(cardShape)
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.CheckCircle, null, tint = SyncGreen, modifier = Modifier.size(26.dp))
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    if (lastSync == null) "No sync yet" else "Last Sync Completed",
                    color = AuthPlum, fontSize = 14.sp, fontWeight = FontWeight.Bold
                )
                Text(lastSync ?: "Tap the button above to start", color = AuthMuted, fontSize = 11.sp)
            }
        }

        Spacer(Modifier.height(14.dp))

        // ===== Connection details =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, cardShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(cardShape)
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                "CONNECTION DETAILS", color = AuthRaspberry, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(4.dp))
            DetailRow("Current Device", Build.MODEL)
            DetailRow("Device Role", if (isOwner) "Owner" else "Employee", pill = true)
            DetailRow("Nearby Device", lastPeer ?: if (isOwner) "Employee phone" else "Owner phone")
            DetailRow("Transport", "TCP Socket")
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun DeviceTile(name: String, role: String, bg: Color, tint: Color) {
    Column(
        modifier = Modifier.width(66.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box {
            Box(
                modifier = Modifier.size(54.dp).clip(RoundedCornerShape(16.dp)).background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.PhoneAndroid, null, tint = tint, modifier = Modifier.size(28.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-3).dp)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(SyncGreen)
                    .border(1.5.dp, Color.White, CircleShape)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(name, color = AuthPlum, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text(role, color = AuthMuted, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun ReadyRow(t: Txn) {
    val isIn = t.qty > 0
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isIn) SyncGreenBg else SyncRedBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isIn) Icons.Outlined.Download else Icons.Outlined.Upload,
                null,
                tint = if (isIn) SyncGreen else SyncRed,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(t.item, color = AuthPlum, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(t.person, color = AuthMuted, fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                if (isIn) "+${t.qty}" else "−${-t.qty}",
                color = if (isIn) SyncGreen else SyncRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(t.time, color = AuthMuted, fontSize = 10.sp)
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, pill: Boolean = false) {
    HorizontalDivider(color = AuthFieldBorder, modifier = Modifier.padding(top = 10.dp))
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AuthMuted, fontSize = 12.sp)
        if (pill) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(OwnerTileBg)
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(value, color = AuthRaspberry, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(value, color = AuthPlum, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}