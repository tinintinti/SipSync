package com.example.sipsync.screens

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
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val InGreen = Color(0xFF1E8E5A)
private val InGreenBg = Color(0xFFDDF2E8)
private val OutRed = Color(0xFFD64545)
private val OutRedBg = Color(0xFFFFE0E6)

@Composable
fun HomeScreen(
    beverages: List<Beverage>,
    isOwner: Boolean,
    userName: String,
    transactions: List<Txn>,
    pendingCount: Int,
    onNavigate: (Int) -> Unit,
    onStockIn: () -> Unit = {},
    onStockOut: () -> Unit = {}
) {

    val lowStock = beverages.count { it.units in 1..it.minStock }
    val outOfStock = beverages.count { it.units == 0 }
    val stockIn = transactions.filter { it.qty > 0 }.sumOf { it.qty }
    val stockOut = transactions.filter { it.qty < 0 }.sumOf { -it.qty }

    val firstName = userName.trim().split(" ").firstOrNull().orEmpty()
    val initials = initialsOf(userName)
    val cardShape = RoundedCornerShape(24.dp)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthBackground)
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(16.dp))

        // ===== Greeting row =====
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(AuthRaspberry, AuthRaspberryDark))),
                contentAlignment = Alignment.Center
            ) {
                Text(initials, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Good morning,", color = AuthMuted, fontSize = 13.sp)
                Text(firstName, color = AuthPlum, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
            }
            AuthLogo(logoSize = 46.dp)
        }

        Spacer(Modifier.height(14.dp))

        // ===== Devices connected pill =====
        val pill = RoundedCornerShape(50)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(pill)
                .background(Color.White.copy(alpha = 0.85f))
                .border(1.dp, AuthFieldBorder, pill)
                .clickable(role = Role.Button) { onNavigate(3) }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(InGreen))
            Spacer(Modifier.width(8.dp))
            Text("Devices connected", color = AuthPlum, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text("· Synced 2 min ago", color = AuthMuted, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                tint = AuthMuted, modifier = Modifier.size(18.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // ===== Stats card =====
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, cardShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(cardShape)
                .background(Color.White)
        ) {
            Row(
                modifier = Modifier.padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatCell("${beverages.size}", "Beverages", null, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(40.dp).background(AuthFieldBorder))
                StatCell("$lowStock", "Low stock", AuthOrange, Modifier.weight(1f))
                Box(Modifier.width(1.dp).height(40.dp).background(AuthFieldBorder))
                StatCell("$outOfStock", "Out of stock", OutRed, Modifier.weight(1f))
            }
            if (isOwner) {
                HorizontalDivider(color = AuthFieldBorder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        Text("This week · Stock in ", color = AuthMuted, fontSize = 11.sp)
                        Text("+$stockIn", color = InGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    Row {
                        Text("Stock out ", color = AuthMuted, fontSize = 11.sp)
                        Text("−$stockOut", color = OutRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(Modifier.height(22.dp))

        // ===== Quick actions =====
        SectionTitle("Quick Actions")
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            QuickAction("Stock In", Icons.Outlined.Download, InGreen, InGreenBg, null, Modifier.weight(1f), onStockIn)
            QuickAction("Stock Out", Icons.Outlined.Upload, OutRed, OutRedBg, null, Modifier.weight(1f), onStockOut)
            if (isOwner) {
                QuickAction("Manage", Icons.Outlined.Edit, AuthOrange, Color(0xFFFFE3C7), null, Modifier.weight(1f)) {
                    onNavigate(1)
                }
            } else {
                QuickAction("View", Icons.Outlined.Visibility, AuthOrange, Color(0xFFFFE3C7), null, Modifier.weight(1f)) {
                    onNavigate(1)
                }
            }
            QuickAction("Sync", Icons.Outlined.Sync, AuthPlum, Color(0xFFF3E1EC), if (pendingCount > 0) "$pendingCount" else null, Modifier.weight(1f)) {
                onNavigate(3)
            }
        }

        Spacer(Modifier.height(22.dp))

        // ===== Recent activity =====
        SectionTitle("Recent Activity", "See all") { onNavigate(2) }
        Spacer(Modifier.height(12.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(6.dp, cardShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                .clip(cardShape)
                .background(Color.White)
        ) {
            val recent = transactions.take(3)
            if (recent.isEmpty()) {
                Text(
                    "No transactions yet",
                    color = AuthMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(24.dp)
                )
            }
            recent.forEachIndexed { index, t ->
                if (index > 0) HorizontalDivider(color = AuthFieldBorder)
                ActivityRow(t)
            }
        }

        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun StatCell(value: String, label: String, dot: Color?, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = AuthPlum, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (dot != null) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(dot))
                Spacer(Modifier.width(4.dp))
            }
            Text(label, color = AuthMuted, fontSize = 11.sp)
        }
    }
}

@Composable
private fun SectionTitle(title: String, action: String? = null, onAction: () -> Unit = {}) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, color = AuthPlum, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        if (action != null) {
            Text(
                action,
                color = AuthRaspberry,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(role = Role.Button, onClick = onAction).padding(4.dp)
            )
        }
    }
}

@Composable
private fun QuickAction(
    label: String,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    badge: String?,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier.clickable(role = Role.Button, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(Modifier.size(62.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(bg),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = tint, modifier = Modifier.size(26.dp))
            }
            if (badge != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 4.dp, y = (-4).dp)
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AuthRaspberry),
                    contentAlignment = Alignment.Center
                ) {
                    Text(badge, color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = AuthPlum, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ActivityRow(t: Txn) {
    val isIn = t.qty > 0
    Row(
        modifier = Modifier.fillMaxWidth().padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isIn) InGreenBg else OutRedBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                if (isIn) Icons.Outlined.Download else Icons.Outlined.Upload,
                null,
                tint = if (isIn) InGreen else OutRed,
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
                color = if (isIn) InGreen else OutRed,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(t.time, color = AuthMuted, fontSize = 10.sp)
        }
    }
}