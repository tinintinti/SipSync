package com.example.sipsync.screens

import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.sipsync.sync.SyncStatus
import com.example.sipsync.sync.WifiDirectSync

/**
 * Shown from the login screen on a phone that has no accounts yet.
 * It lets the Employee phone pull the employee account (and the beverage list)
 * from the Owner's phone over Wi-Fi Direct, so the employee can log in.
 */
@Composable
fun GetAccountSyncDialog(
    status: SyncStatus,
    onStart: () -> Unit,
    onSyncNow: () -> Unit,
    onStop: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val required = WifiDirectSync.requiredPermissions()
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result -> if (result.values.all { it }) onStart() }

    LaunchedEffect(Unit) {
        val granted = required.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) onStart() else launcher.launch(required)
    }
    DisposableEffect(Unit) { onDispose { onStop() } }

    val busy = status is SyncStatus.Searching ||
        status is SyncStatus.Connecting || status is SyncStatus.Syncing
    val done = status is SyncStatus.Done

    val message = when (status) {
        is SyncStatus.Idle -> "Open the Sync screen on the Owner's phone. This phone will find it and sync by itself in a few seconds. You can also tap the button below."
        is SyncStatus.Searching -> "Looking for the Owner's phone..."
        is SyncStatus.Connecting -> "Connecting to ${status.peer}. Accept the connection request on the Owner's phone if it asks."
        is SyncStatus.Syncing -> "Syncing with ${status.peer}..."
        is SyncStatus.Done -> "Done. The employee account is now on this phone. Close this and log in."
        is SyncStatus.Failed -> status.reason
    }

    AlertDialog(
        onDismissRequest = { if (!busy) onClose() },
        title = {
            Text("Get account from Owner", color = AuthPlum, fontWeight = FontWeight.ExtraBold)
        },
        text = {
            Column {
                Text(message, color = AuthMuted, fontSize = 13.sp)
                if (!done) {
                    Spacer(Modifier.height(16.dp))
                    Button(
                        onClick = onSyncNow,
                        enabled = !busy,
                        colors = ButtonDefaults.buttonColors(containerColor = AuthRaspberry)
                    ) {
                        Text(if (busy) "Please wait..." else "Find Owner phone")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onClose) {
                Text(if (done) "Close" else "Cancel", color = AuthRaspberry, fontWeight = FontWeight.Bold)
            }
        }
    )
}
