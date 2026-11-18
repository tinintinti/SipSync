package com.example.sipsync.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(beverages: List<Beverage>, isOwner: Boolean, userName: String, onNavigate: (Int) -> Unit) {
    val context = LocalContext.current
    val transactions = transactionsFor(isOwner)
    val lowStock = beverages.count { it.units in 1..it.minStock }
    val outOfStock = beverages.count { it.units == 0 }
    val stockIn = transactions.filter { it.qty > 0 }.sumOf { it.qty }
    val stockOut = transactions.filter { it.qty < 0 }.sumOf { -it.qty }

    val firstName = userName.trim().split(" ").firstOrNull().orEmpty()
    val initials = initialsOf(userName)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

        // ===== Dark header =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(SipDeepTeal)
        ) {
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MiniLogo(onDark = true)
                    Avatar(initials, 44.dp)
                }
                Spacer(Modifier.height(16.dp))
                Text("Good morning,", color = SipMintSoft, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(firstName, color = Color.White, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(8.dp))
                    StatusDot(SipMintBright, 8.dp)
                }
                Spacer(Modifier.height(14.dp))

                // Devices connected pill
                val pill = RoundedCornerShape(14.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(pill)
                        .background(Color.White.copy(alpha = 0.08f))
                        .border(1.dp, Color.White.copy(alpha = 0.12f), pill)
                        .clickable { onNavigate(3) }
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusDot(SipMintBright, 7.dp)
                    Spacer(Modifier.width(10.dp))
                    Text("Devices connected", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                    Text("Synced 2 min ago", color = SipMintSoft, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = SipMintSoft,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // ===== Body =====
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            SectionHeader("OVERVIEW", "Inventory at a glance", "View all") { onNavigate(1) }

            // Total beverages
            SipCard(modifier = Modifier.fillMaxWidth(), onClick = { onNavigate(1) }) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconTile(Icons.Outlined.Inventory2, SipTeal, SipMint, 46.dp)
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("${beverages.size}", color = SipDeepTeal, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Text("Total beverages", color = SipMuted, fontSize = 12.sp)
                    }
                    Chevron()
                }
            }

            // Low stock + out of stock
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CountCard("Low stock", lowStock, Icons.Outlined.WarningAmber, SipAmber, SipAmberBg, Modifier.weight(1f))
                CountCard("Out of stock", outOfStock, Icons.Filled.Remove, SipRed, SipRedBg, Modifier.weight(1f))
            }

            // Weekly movement (Owner only)
            if (isOwner) {
                SipCard(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        FlowStat("Stock in", "+$stockIn", Icons.Outlined.Download, SipTeal, SipMint, Modifier.weight(1f))
                        Box(Modifier.width(1.dp).height(34.dp).background(SipBorder))
                        Spacer(Modifier.width(14.dp))
                        FlowStat("Stock out", "−$stockOut", Icons.Outlined.Upload, SipRed, SipRedBg, Modifier.weight(1f))
                    }
                }
            }

            Spacer(Modifier.height(6.dp))
            SectionHeader("QUICK ACTIONS", "What do you need?")

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ActionCard("Stock In", "Receive items", Icons.Outlined.Download, SipTeal, SipMint, Modifier.weight(1f)) {
                    Toast.makeText(context, "Stock In form comes next", Toast.LENGTH_SHORT).show()
                }
                ActionCard("Stock Out", "Release items", Icons.Outlined.Upload, SipRed, SipRedBg, Modifier.weight(1f)) {
                    Toast.makeText(context, "Stock Out form comes next", Toast.LENGTH_SHORT).show()
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (isOwner) {
                    ActionCard("Manage Inventory", "Update products", Icons.Outlined.Edit, SipBlue, SipBlueBg, Modifier.weight(1f)) {
                        onNavigate(1)
                    }
                } else {
                    ActionCard("View Inventory", "Check quantities", Icons.Outlined.Visibility, SipBlue, SipBlueBg, Modifier.weight(1f)) {
                        onNavigate(1)
                    }
                }
                ActionCard("Synchronize", "1 pending", Icons.Outlined.Sync, SipAmber, SipAmberBg, Modifier.weight(1f)) {
                    onNavigate(3)
                }
            }

            Spacer(Modifier.height(6.dp))
            SectionHeader("RECENT ACTIVITY", "Latest transactions", "See all") { onNavigate(2) }
            transactions.take(3).forEach { TransactionRow(it) }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CountCard(
    label: String,
    count: Int,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    modifier: Modifier
) {
    SipCard(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(icon, tint, bg, 44.dp)
                Spacer(Modifier.width(12.dp))
                Text("$count", color = SipDeepTeal, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.height(10.dp))
            Text(label, color = SipMuted, fontSize = 12.sp)
        }
    }
}

@Composable
private fun FlowStat(
    label: String,
    value: String,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    modifier: Modifier
) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        IconTile(icon, tint, bg, 34.dp)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, color = SipDeepTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("This week", color = SipLight, fontSize = 9.sp)
        }
        Text(value, color = SipTeal, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.width(10.dp))
    }
}

@Composable
private fun ActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tint: Color,
    bg: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    SipCard(modifier = modifier, onClick = onClick) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            IconTile(icon, tint, bg, 42.dp)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(title, color = SipDeepTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = SipMuted, fontSize = 10.sp)
            }
        }
    }
}