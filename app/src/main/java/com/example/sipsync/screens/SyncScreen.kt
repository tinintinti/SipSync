package com.example.sipsync.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SyncScreen(isOwner: Boolean) {
    var pending by remember { mutableStateOf(1) }
    var lastSync by remember { mutableStateOf("2 min ago") }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("WI-FI DIRECT", "Device Sync")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ===== Connection status card =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF0C5551), SipDeepTeal)))
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Wifi, null, tint = SipMintBright, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("CONNECTION STATUS", color = SipMintSoft, fontSize = 9.sp, letterSpacing = 1.sp)
                        Text("Connected", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isOwner) {
                        DeviceTile("Owner phone", "This device")
                    } else {
                        DeviceTile("Employee phone", "This device")
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            repeat(3) { StatusDot(Color.White.copy(alpha = 0.4f), 6.dp) }
                        }
                        Spacer(Modifier.height(6.dp))
                        Text("Wi-Fi Direct", color = SipMintSoft, fontSize = 9.sp)
                    }
                    if (isOwner) {
                        DeviceTile("Employee phone", "Authorized peer")
                    } else {
                        DeviceTile("Owner phone", "Authorized peer")
                    }
                }

                Spacer(Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.12f))
                Spacer(Modifier.height(12.dp))
                Text(
                    "Direct, secure device-to-device connection. No internet required.",
                    color = SipMintSoft,
                    fontSize = 10.sp,
                    textAlign = TextAlign.Center
                )
            }

            // ===== Pending + last sync =====
            SipCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.Receipt, SipTeal, SipMint, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Pending transactions", color = SipDeepTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Ready to exchange", color = SipMuted, fontSize = 10.sp)
                    }
                    Text("$pending", color = SipTeal, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            SipCard(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.Sync, SipTeal, SipMint, 40.dp)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Last synchronization", color = SipDeepTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Completed successfully", color = SipMuted, fontSize = 10.sp)
                    }
                    Text(lastSync, color = SipDeepTeal, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // ===== Connection details =====
            SipCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Kicker("CONNECTION DETAILS")
                    Spacer(Modifier.height(4.dp))
                    Text("Authorized devices", color = SipDeepTeal, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.height(10.dp))
                    DetailRow("Current device", if (isOwner) "Mara's Galaxy A54" else "Store Galaxy A34")
                    DetailRow("Device role", if (isOwner) "Owner" else "Employee", pill = true)
                    DetailRow("Nearby device", if (isOwner) "Store Galaxy A34" else "Mara's Galaxy A54")
                    DetailRow("Transport", "TCP socket")
                }
            }

            // ===== Info box =====
            val infoShape = RoundedCornerShape(16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(infoShape)
                    .background(Color(0xFFFFF6E5))
                    .border(1.dp, Color(0xFFF5E3BC), infoShape)
                    .padding(14.dp)
            ) {
                Icon(Icons.Outlined.Info, null, tint = SipAmber, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("How SipSync syncs", color = Color(0xFF8A5A12), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "Pending inventory transactions are exchanged directly over Wi-Fi Direct using a local TCP socket. Your data never goes to the cloud.",
                        color = Color(0xFF8A6A3A),
                        fontSize = 10.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            // ===== Sync button (prototype: just clears the pending count) =====
            Button(
                onClick = {
                    pending = 0
                    lastSync = "Just now"
                },
                enabled = pending > 0,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SipTeal,
                    contentColor = Color.White,
                    disabledContainerColor = SipMint,
                    disabledContentColor = SipTeal
                )
            ) {
                Icon(Icons.Outlined.Sync, null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    text = if (pending > 0) "Synchronize $pending transaction" else "Everything is synced",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                "Also supports: Disconnected · Searching · Connecting · Sync failed",
                color = SipLight,
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DeviceTile(name: String, role: String) {
    val shape = RoundedCornerShape(18.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(shape)
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.25f), shape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.PhoneAndroid, null, tint = Color.White, modifier = Modifier.size(32.dp))
            StatusDot(SipMintBright, 8.dp, Modifier.align(Alignment.TopEnd).padding(7.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(name, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Text(role, color = SipMintSoft, fontSize = 9.sp)
    }
}

@Composable
private fun DetailRow(label: String, value: String, pill: Boolean = false) {
    HorizontalDivider(color = SipBorder)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = SipMuted, fontSize = 12.sp)
        if (pill) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(SipMint)
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Text(value, color = SipTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            Text(value, color = SipDeepTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}