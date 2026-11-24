package com.example.sipsync.screens

import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val OkGreen = Color(0xFF1E8E5A)
private val OkGreenBg = Color(0xFFDDF2E8)
private val NoMark = Color(0xFFB9A7B0)
private val NoMarkBg = Color(0xFFF3ECEF)

// ---------- Shared page frame ----------
@Composable
private fun SubPage(
    kicker: String,
    title: String,
    onBack: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color.White.copy(alpha = 0.85f))
                    .clickable(role = Role.Button, onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back", tint = AuthPlum)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    kicker, color = AuthRaspberry, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                )
                Text(title, color = AuthPlum, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
        Spacer(Modifier.height(18.dp))
        content()
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun WhiteCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
            .clip(shape)
            .background(Color.White)
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun InfoRow(label: String, value: String, first: Boolean = false) {
    if (!first) HorizontalDivider(color = AuthFieldBorder)
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AuthMuted, fontSize = 12.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            value, color = AuthPlum, fontSize = 12.sp, fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End, modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NoteBox(text: String) {
    Text(
        text,
        color = AuthMuted,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AuthFieldBg)
            .padding(12.dp)
    )
}

// =====================================================================
// 1. Role Permissions
// =====================================================================
private data class Perm(
    val label: String,
    val owner: Boolean,
    val employee: Boolean,
    val sub: String? = null
)

@Composable
fun RolePermissionsScreen(isOwner: Boolean, onBack: () -> Unit) {
    val perms = listOf(
        Perm("View inventory and stock levels", true, true),
        Perm("Record Stock In", true, true),
        Perm("Record Stock Out", true, true),
        Perm("View transaction history", true, true, "Owner: all devices · Employee: own activity"),
        Perm("Sync with the other device", true, true),
        Perm("Change own password", true, true),
        Perm("Add and delete beverages", true, false),
        Perm("Create the employee account", true, false),
        Perm("Edit own profile name", true, false)
    )

    SubPage("YOUR ACCESS", "Role Permissions", onBack) {
        // Current role banner
        val banner = RoundedCornerShape(20.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(banner)
                .background(Brush.horizontalGradient(listOf(AuthRaspberry, AuthRaspberryDark)))
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Signed in as ${if (isOwner) "Owner" else "Employee"}",
                    color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                )
                Text(
                    if (isOwner) "Full access to inventory, history and accounts"
                    else "Daily operations: record stock and view inventory",
                    color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        WhiteCard {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PERMISSION", color = AuthMuted, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.sp,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "Owner", fontSize = 11.sp, textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = if (isOwner) AuthRaspberry else AuthMuted,
                    modifier = Modifier.width(64.dp)
                )
                Text(
                    "Employee", fontSize = 11.sp, textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = if (!isOwner) AuthRaspberry else AuthMuted,
                    modifier = Modifier.width(64.dp)
                )
            }
            perms.forEach { p ->
                HorizontalDivider(color = AuthFieldBorder)
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(p.label, color = AuthPlum, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        if (p.sub != null) Text(p.sub, color = AuthMuted, fontSize = 10.sp)
                    }
                    AccessMark(p.owner)
                    AccessMark(p.employee)
                }
            }
        }
    }
}

@Composable
private fun AccessMark(allowed: Boolean) {
    Box(modifier = Modifier.width(64.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(if (allowed) OkGreenBg else NoMarkBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (allowed) Icons.Default.Check else Icons.Default.Close,
                contentDescription = if (allowed) "Allowed" else "Not allowed",
                tint = if (allowed) OkGreen else NoMark,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

// =====================================================================
// 2. This Android Device
// =====================================================================
@Composable
fun DeviceInfoScreen(isOwner: Boolean, deviceId: String, onBack: () -> Unit) {
    val context = LocalContext.current
    val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }
    val wifiDirect = context.packageManager.hasSystemFeature(PackageManager.FEATURE_WIFI_DIRECT)
    val appVersion = remember {
        runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        }.getOrNull() ?: "1.0"
    }

    SubPage("THIS PHONE", "This Android Device", onBack) {
        WhiteCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.linearGradient(listOf(AuthRaspberry, AuthOrange))),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.PhoneAndroid, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "$manufacturer ${Build.MODEL}", color = AuthPlum,
                        fontSize = 16.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        if (isOwner) "Owner phone" else "Employee phone",
                        color = AuthRaspberry, fontSize = 12.sp, fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = AuthFieldBorder)
            InfoRow("Role on this device", if (isOwner) "Owner" else "Employee", first = true)
            InfoRow("Android version", "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
            InfoRow("Wi-Fi Direct", if (wifiDirect) "Supported" else "Not supported")
            InfoRow("App version", "SipSync $appVersion")
            InfoRow("Device ID", deviceId.take(8).uppercase() + "…")
        }

        Spacer(Modifier.height(14.dp))
        NoteBox(
            "The device ID is created once on this phone. It marks the transactions recorded here " +
                "so the other phone can tell them apart when you sync."
        )
        if (!wifiDirect) {
            Spacer(Modifier.height(10.dp))
            Text(
                "This phone does not support Wi-Fi Direct, so it cannot sync with the other device.",
                color = Color(0xFFD64545), fontSize = 11.sp, lineHeight = 16.sp
            )
        }
    }
}

// =====================================================================
// 3. Offline Data
// =====================================================================
@Composable
fun OfflineDataScreen(
    beverageCount: Int,
    transactionCount: Int,
    pendingCount: Int,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val storage = remember(beverageCount, transactionCount) {
        val bytes = context.databaseList().sumOf { context.getDatabasePath(it).length() }
        when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
            else -> "%.1f MB".format(bytes / (1024.0 * 1024.0))
        }
    }

    SubPage("YOUR DATA", "Offline Data", onBack) {
        WhiteCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp)).background(OkGreenBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Outlined.Lock, null, tint = OkGreen, modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Stored on this phone only", color = AuthPlum,
                        fontSize = 15.sp, fontWeight = FontWeight.ExtraBold
                    )
                    Text("Works with no internet connection", color = AuthMuted, fontSize = 11.sp)
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("$beverageCount", "Beverages", Modifier.weight(1f))
            StatTile("$transactionCount", "Transactions", Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("$pendingCount", "Waiting to sync", Modifier.weight(1f))
            StatTile(storage, "Storage used", Modifier.weight(1f))
        }

        Spacer(Modifier.height(14.dp))

        WhiteCard {
            Text(
                "HOW YOUR DATA IS HANDLED", color = AuthRaspberry, fontSize = 10.sp,
                fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(6.dp))
            InfoRow("Where it lives", "In a database on this phone", first = true)
            InfoRow("Internet needed", "No")
            InfoRow("Shared with", "The authorized device, over Wi-Fi Direct")
            InfoRow("Passwords", "Stored as salted hashes")
        }

        Spacer(Modifier.height(14.dp))
        NoteBox(
            "Transactions counted here are the ones this account can see. Uninstalling the app or " +
                "registering a new store erases this data from the phone."
        )
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        modifier = modifier
            .shadow(5.dp, shape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
            .clip(shape)
            .background(Color.White)
            .padding(16.dp)
    ) {
        Text(value, color = AuthPlum, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = AuthMuted, fontSize = 11.sp)
    }
}

// =====================================================================
// 4. Edit Profile (Owner: display name only)
// =====================================================================
@Composable
fun EditProfileScreen(currentName: String, onSave: (String) -> Unit, onBack: () -> Unit) {
    val context = LocalContext.current
    DarkStatusBarIcons()
    var name by remember { mutableStateOf(currentName) }

    SubPage("OWNER ONLY", "Edit Profile", onBack) {
        AuthCard {
            AuthLabel("Full name")
            AuthField(name, { name = it }, "e.g. Mara Santos", Icons.Outlined.Person)

            Spacer(Modifier.height(14.dp))
            NoteBox(
                "Your username cannot be changed because it is used to sign in and to identify " +
                    "your account when the phones sync."
            )

            Spacer(Modifier.height(20.dp))
            AuthPrimaryButton(text = "Save changes", onClick = {
                when {
                    name.trim().length < 2 ->
                        Toast.makeText(context, "Please enter your full name", Toast.LENGTH_SHORT).show()
                    else -> {
                        onSave(name.trim())
                        Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                }
            })
        }
    }
}

// =====================================================================
// 5. Change Password (both roles)
// =====================================================================
@Composable
fun ChangePasswordScreen(
    onChange: (current: String, newPassword: String, onResult: (Boolean) -> Unit) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    DarkStatusBarIcons()
    var current by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var visible by remember { mutableStateOf(false) }

    fun submit() {
        val error: String? = when {
            current.isEmpty() -> "Please enter your current password"
            newPass.length < 6 -> "New password must be at least 6 characters"
            newPass == current -> "New password must be different"
            newPass != confirm -> "New passwords do not match"
            else -> null
        }
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            return
        }
        onChange(current, newPass) { ok ->
            if (ok) {
                Toast.makeText(context, "Password changed", Toast.LENGTH_SHORT).show()
                onBack()
            } else {
                Toast.makeText(context, "Current password is incorrect", Toast.LENGTH_SHORT).show()
            }
        }
    }

    SubPage("SECURITY", "Change Password", onBack) {
        AuthCard {
            AuthLabel("Current password")
            AuthField(
                current, { current = it }, "Current password", Icons.Outlined.Lock,
                isPassword = true, passwordVisible = visible, onTogglePassword = { visible = !visible }
            )
            Spacer(Modifier.height(14.dp))
            AuthLabel("New password")
            AuthField(
                newPass, { newPass = it }, "At least 6 characters", Icons.Outlined.Lock,
                isPassword = true, passwordVisible = visible, onTogglePassword = { visible = !visible }
            )
            Spacer(Modifier.height(14.dp))
            AuthLabel("Confirm new password")
            AuthField(
                confirm, { confirm = it }, "Re-enter new password", Icons.Outlined.Lock,
                isPassword = true, passwordVisible = visible, onTogglePassword = { visible = !visible }
            )

            Spacer(Modifier.height(16.dp))
            NoteBox(
                "There is no password recovery because SipSync works offline. Keep your new " +
                    "password somewhere safe."
            )

            Spacer(Modifier.height(20.dp))
            AuthPrimaryButton(text = "Update password", onClick = { submit() })
        }
    }
}