package com.example.sipsync.screens

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val ProfGreen = Color(0xFF1E8E5A)
private val ProfGreenBg = Color(0xFFDDF2E8)
private val ProfLogout = Color(0xFFD64545)

private data class ProfileTile(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val tint: Color,
    val bg: Color,
    val dimmed: Boolean = false, // greyed out (still tappable)
    val onClick: () -> Unit
)

@Composable
fun ProfileScreen(
    isOwner: Boolean,
    userName: String,
    deviceId: String,
    beverageCount: Int,
    transactionCount: Int,
    pendingCount: Int,
    onAddEmployeeClick: () -> Unit,
    onUpdateName: (String) -> Unit,
    onChangePassword: (current: String, newPassword: String, onResult: (Boolean) -> Unit) -> Unit,
    onLogout: () -> Unit,
    employees: List<EmployeeInfo> = emptyList(), // existing employee accounts
    onDeleteEmployee: (String) -> Unit = {}
) {
    // Which sub-page is open inside the Profile tab (null = the profile itself)
    var page by remember { mutableStateOf<String?>(null) }
    BackHandler(enabled = page != null) { page = null }

    val open = page
    if (open != null) {
        val close = { page = null }
        when (open) {
            "permissions" -> RolePermissionsScreen(isOwner, close)
            "device" -> DeviceInfoScreen(isOwner, deviceId, close)
            "offline" -> OfflineDataScreen(beverageCount, transactionCount, pendingCount, close)
            "edit" -> EditProfileScreen(userName, onUpdateName, close)
            "password" -> ChangePasswordScreen(onChangePassword, close)
            "employee" -> {
                val emp = employees.firstOrNull()
                if (emp == null) {
                    LaunchedEffect(Unit) { page = null } // nothing to manage any more
                } else {
                    ManageEmployeeScreen(
                        employee = emp,
                        onDelete = {
                            onDeleteEmployee(emp.id)
                            page = null
                        },
                        onBack = close
                    )
                }
            }
        }
        return
    }

    val initials = initialsOf(userName)
    val roleLine = if (isOwner) "Owner · SipSync Convenience" else "Employee · SipSync Convenience"
    val permissions = if (isOwner) "Full Inventory Management" else "Stock Operations & View Access"
    val cardShape = RoundedCornerShape(24.dp)
    val employee = employees.firstOrNull()

    val tiles = buildList {
        add(
            ProfileTile(
                "Role Permissions", permissions, Icons.Outlined.Shield,
                AuthRaspberry, Color(0xFFFFE0E8)
            ) { page = "permissions" }
        )
        add(
            ProfileTile(
                "This Android Device", Build.MODEL, Icons.Outlined.PhoneAndroid,
                AuthOrange, Color(0xFFFFE3C7)
            ) { page = "device" }
        )
        add(
            ProfileTile(
                "Offline Data", "Stored Securely on this Device", Icons.Outlined.Lock,
                ProfGreen, ProfGreenBg
            ) { page = "offline" }
        )
        if (isOwner) {
            if (employee == null) {
                add(
                    ProfileTile(
                        "Add Employee", "Create an Account for Your Staff", Icons.Outlined.PersonAdd,
                        AuthPlum, Color(0xFFF3E1EC), onClick = onAddEmployeeClick
                    )
                )
            } else {
                // The one employee exists: open the page where the Owner can delete it
                add(
                    ProfileTile(
                        "Manage Employee", "${employee.fullName} · @${employee.username}", Icons.Outlined.Person,
                        AuthPlum, Color(0xFFF3E1EC), onClick = { page = "employee" }
                    )
                )
            }
        }
        add(
            ProfileTile(
                "Change Password", "Keep Your Account Secure", Icons.Outlined.VpnKey,
                AuthRaspberry, Color(0xFFFFE0E8)
            ) { page = "password" }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))
        Text(
            "AUTHORIZED ACCOUNT", color = AuthRaspberry, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
        )
        Text("Profile", color = AuthPlum, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)

        Spacer(Modifier.height(14.dp))

        // ===== Profile card =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, cardShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(cardShape)
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier.padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(72.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(AuthRaspberry, AuthOrange))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initials, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(ProfGreen)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(userName, color = AuthPlum, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                    Text(roleLine, color = AuthMuted, fontSize = 12.sp)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ProfGreenBg)
                            .padding(horizontal = 12.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, null, tint = ProfGreen, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "Authorized device", color = ProfGreen,
                            fontSize = 11.sp, fontWeight = FontWeight.Bold
                        )
                    }
                }
                // Owner can edit their display name
                if (isOwner) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFE0E8))
                            .clickable(role = Role.Button) { page = "edit" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Edit, "Edit profile", tint = AuthRaspberry, modifier = Modifier.size(18.dp))
                    }
                }
            }
            HorizontalDivider(color = AuthFieldBorder, modifier = Modifier.padding(horizontal = 18.dp))
            Row(
                modifier = Modifier.padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ProfileStat(if (isOwner) "Owner" else "Employee", "Role", Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(36.dp).background(AuthFieldBorder))
                ProfileStat(if (isOwner) "1 of 2" else "2 of 2", "Devices", Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(36.dp).background(AuthFieldBorder))
                ProfileStat("Offline", "Mode", Modifier.weight(1f))
            }
        }

        Spacer(Modifier.height(22.dp))
        Text(
            "ACCOUNT & DEVICE", color = AuthRaspberry, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
        )
        Spacer(Modifier.height(10.dp))

        // ===== 2-column tile grid =====
        tiles.chunked(2).forEach { pair ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { tile ->
                    TileCard(tile, Modifier.weight(1f).fillMaxHeight())
                }
                if (pair.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }

        // ===== Log out =====
        val logoutShape = RoundedCornerShape(20.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, logoutShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(logoutShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFF5C9CF), logoutShape)
                .clickable(role = Role.Button, onClick = onLogout)
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = ProfLogout, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Log out", color = ProfLogout, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "SipSync · Version 1.0",
            color = AuthMuted,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun ProfileStat(value: String, label: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = AuthPlum, fontSize = 17.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = AuthMuted, fontSize = 11.sp)
    }
}

@Composable
private fun TileCard(tile: ProfileTile, modifier: Modifier) {
    val shape = RoundedCornerShape(22.dp)
    Column(
        modifier = modifier
            .alpha(if (tile.dimmed) 0.6f else 1f)
            .shadow(5.dp, shape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
            .clip(shape)
            .background(Color.White)
            .clickable(role = Role.Button, onClick = tile.onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(tile.bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(tile.icon, null, tint = tile.tint, modifier = Modifier.size(20.dp))
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                tint = AuthMuted, modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(tile.title, color = AuthPlum, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(2.dp))
        Text(tile.subtitle, color = AuthMuted, fontSize = 11.sp, lineHeight = 15.sp)
    }
}

// Owner-only page: see the employee account and delete it
@Composable
private fun ManageEmployeeScreen(
    employee: EmployeeInfo,
    onDelete: () -> Unit,
    onBack: () -> Unit
) {
    var confirming by remember { mutableStateOf(false) }
    val cardShape = RoundedCornerShape(24.dp)
    val backShape = RoundedCornerShape(14.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // Header with back button
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(2.dp, backShape)
                    .clip(backShape)
                    .background(Color.White)
                    .clickable(role = Role.Button, onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back", tint = AuthPlum)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "OWNER MANAGEMENT", color = AuthRaspberry, fontSize = 10.sp,
                    fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                )
                Text("Employee Account", color = AuthPlum, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        Spacer(Modifier.height(20.dp))

        // Employee card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, cardShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(cardShape)
                .background(Color.White)
                .padding(vertical = 24.dp, horizontal = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AuthRaspberry, AuthOrange))),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    initialsOf(employee.fullName), color = Color.White,
                    fontSize = 22.sp, fontWeight = FontWeight.ExtraBold
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(employee.fullName, color = AuthPlum, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text("@${employee.username}", color = AuthMuted, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFFFFE3C7))
                    .padding(horizontal = 14.dp, vertical = 5.dp)
            ) {
                Text("Employee", color = AuthOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "Can record Stock In and Stock Out and view their own history. Cannot add, edit or delete beverages.",
            color = AuthMuted, fontSize = 12.sp, lineHeight = 17.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(Modifier.height(22.dp))

        // Delete button
        val deleteShape = RoundedCornerShape(20.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(4.dp, deleteShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(deleteShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFF5C9CF), deleteShape)
                .clickable(role = Role.Button) { confirming = true }
                .padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Outlined.Delete, null, tint = ProfLogout, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text("Delete employee", color = ProfLogout, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(10.dp))
        Text(
            "The account is removed from this phone. The employee's phone is signed out the next time the two phones sync. Past transactions stay in History.",
            color = AuthMuted, fontSize = 10.sp, lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
    }

    if (confirming) {
        AlertDialog(
            onDismissRequest = { confirming = false },
            title = { Text("Delete ${employee.fullName}?", color = AuthPlum, fontWeight = FontWeight.ExtraBold) },
            text = {
                Text(
                    "They will no longer be able to log in. Their past transactions stay in History. You can then create a new employee account.",
                    color = AuthMuted, fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirming = false
                    onDelete()
                }) {
                    Text("Delete", color = ProfLogout, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirming = false }) {
                    Text("Cancel", color = AuthMuted)
                }
            }
        )
    }
}