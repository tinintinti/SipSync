package com.example.sipsync.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    isOwner: Boolean,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    fun soon(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    val initials = if (isOwner) "MS" else "NR"
    val fullName = if (isOwner) "Mara Santos" else "Nico Reyes"
    val roleLine = if (isOwner) "Owner · SipSync Convenience" else "Employee · SipSync Convenience"
    val permissions = if (isOwner) "Full inventory management" else "Stock operations & view access"
    val deviceName = if (isOwner) "Mara's Galaxy A54" else "Store Galaxy A34"
    val deviceCount = if (isOwner) "1 of 2" else "2 of 2"
    val switchText = if (isOwner) "Preview the Employee experience" else "Preview the Owner experience"

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader("AUTHORIZED ACCOUNT", "Profile")

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Profile card
            SipCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Avatar(initials, 80.dp)
                    Spacer(Modifier.height(12.dp))
                    Text(fullName, color = SipDeepTeal, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                    Text(roleLine, color = SipMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(SipMint)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, null, tint = SipTeal, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(5.dp))
                        Text("Authorized device", color = SipTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Kicker("ACCOUNT & DEVICE")

            SipCard(modifier = Modifier.fillMaxWidth()) {
                Column {
                    ListRow(Icons.Outlined.Person, "Role permissions", permissions,
                        onClick = { soon("Permissions screen comes later") }) { Chevron() }
                    HorizontalDivider(color = SipBorder)
                    ListRow(Icons.Outlined.PhoneAndroid, "This Android device", deviceName,
                        onClick = {}) {
                        Text(deviceCount, color = SipTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    HorizontalDivider(color = SipBorder)
                    ListRow(Icons.Outlined.Lock, "Offline data", "Stored securely on this device",
                        onClick = { soon("Offline data details come later") }) { Chevron() }
                }
            }

            SipCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSwitchRole
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Sync, null, tint = SipTeal, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Switch prototype role", color = SipDeepTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(switchText, color = SipMuted, fontSize = 10.sp)
                    }
                }
            }

            // Log out
            val logoutShape = RoundedCornerShape(16.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(logoutShape)
                    .background(SipRedBg)
                    .border(1.dp, Color(0xFFF7D3D3), logoutShape)
                    .clickable(onClick = onLogout)
                    .padding(vertical = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, null, tint = SipRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Log out", color = SipRed, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Text(
                "SipSync prototype · Version 1.0",
                color = SipLight,
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ListRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon, SipTeal, SipMint, 38.dp)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = SipDeepTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = SipMuted, fontSize = 10.sp)
        }
        trailing()
    }
}