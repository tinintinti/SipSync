package com.example.sipsync.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- Palette for this screen (pink/peach design) ----
private val HistPink = Color(0xFFC2366B)
private val HistDeep = Color(0xFF5B1A33)
private val HistMuted = Color(0xFF7D6470)
private val HistLight = Color(0xFFA38C96)
private val HistDivider = Color(0xFFF6E7EA)
private val HistGreen = Color(0xFF1E7A57)
private val HistGreenBg = Color(0xFFDDF3E7)
private val HistRed = Color(0xFFD64545)
private val HistRedBg = Color(0xFFFCE3E3)

private val HistBackground = Brush.verticalGradient(
    listOf(Color(0xFFFFC9B0), Color(0xFFFDE0D6), Color(0xFFFEF4F0))
)

// time strings look like "Today, 8:41 PM", "Yesterday, 8:41 PM" or "Nov 14, 6:41 PM"
private fun dayOf(time: String) = if (time.contains(", ")) time.substringBeforeLast(", ") else time
private fun clockOf(time: String) = if (time.contains(", ")) time.substringAfterLast(", ") else ""

@Composable
fun HistoryScreen(isOwner: Boolean, transactions: List<Txn>) {
    var filter by remember { mutableStateOf(0) } // 0 = All, 1 = Stock In, 2 = Stock Out
    var dateFilter by remember { mutableStateOf<String?>(null) }
    var dateMenuOpen by remember { mutableStateOf(false) }

    val all = transactions
    val dates = all.map { dayOf(it.time) }.distinct()
    val activeDate = dateFilter?.takeIf { it in dates }

    val shown = all.filter {
        (filter == 0 || (filter == 1 && it.qty > 0) || (filter == 2 && it.qty < 0)) &&
            (activeDate == null || dayOf(it.time) == activeDate)
    }
    val groups = shown.groupBy { dayOf(it.time) } // keeps newest-first order

    Column(modifier = Modifier.fillMaxSize().background(HistBackground)) {

        // ===== Header =====
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
        ) {
            Text(
                if (isOwner) "ALL DEVICE ACTIVITY" else "YOUR ACTIVITY",
                color = HistPink,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
            Spacer(Modifier.height(4.dp))
            Text("Transaction History", color = HistDeep, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Devices + record count pill
            val pillShape = RoundedCornerShape(50)
            Row(
                modifier = Modifier
                    .shadow(2.dp, pillShape)
                    .clip(pillShape)
                    .background(Color.White)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusDot(HistGreen, 8.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    if (isOwner) "Both Authorized Devices" else "Employee Device",
                    color = HistDeep,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("  •  ${all.size} Local Records", color = HistMuted, fontSize = 12.sp)
            }

            // Segmented filter: All / Stock In / Stock Out
            val segShape = RoundedCornerShape(20.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(3.dp, segShape)
                    .clip(segShape)
                    .background(Color.White)
                    .padding(5.dp)
            ) {
                listOf("All", "Stock In", "Stock Out").forEachIndexed { index, label ->
                    val selected = filter == index
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (selected) HistPink else Color.Transparent)
                            .clickable { filter = index },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            color = if (selected) Color.White else HistDeep,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Date dropdown + record count
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    val dateShape = RoundedCornerShape(14.dp)
                    Row(
                        modifier = Modifier
                            .shadow(2.dp, dateShape)
                            .clip(dateShape)
                            .background(Color.White)
                            .clickable { dateMenuOpen = true }
                            .padding(start = 14.dp, end = 8.dp, top = 9.dp, bottom = 9.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(activeDate ?: "All Dates", color = HistDeep, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Icon(Icons.Default.KeyboardArrowDown, null, tint = HistMuted, modifier = Modifier.size(18.dp))
                    }
                    DropdownMenu(expanded = dateMenuOpen, onDismissRequest = { dateMenuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("All Dates") },
                            onClick = { dateFilter = null; dateMenuOpen = false }
                        )
                        dates.forEach { d ->
                            DropdownMenuItem(
                                text = { Text(d) },
                                onClick = { dateFilter = d; dateMenuOpen = false }
                            )
                        }
                    }
                }
                Text(
                    "${shown.size} RECORDS",
                    color = HistMuted,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
            }

            if (shown.isEmpty()) {
                Text(
                    "No transactions yet",
                    color = HistMuted,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    textAlign = TextAlign.Center
                )
            }

            // One card per day, with a divider between rows
            groups.forEach { (day, items) ->
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        day.uppercase(),
                        color = HistMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                    val cardShape = RoundedCornerShape(20.dp)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, cardShape)
                            .clip(cardShape)
                            .background(Color.White)
                    ) {
                        items.forEachIndexed { i, t ->
                            if (i > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                        .height(1.dp)
                                        .background(HistDivider)
                                )
                            }
                            HistoryRow(t)
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HistoryRow(t: Txn) {
    val isIn = t.qty > 0
    val accent = if (isIn) HistGreen else HistRed
    val accentBg = if (isIn) HistGreenBg else HistRedBg

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Item tile with a small direction badge
        Box(modifier = Modifier.size(52.dp)) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.LocalDrink, null, tint = accent, modifier = Modifier.size(24.dp))
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(accent),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIn) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    null,
                    tint = Color.White,
                    modifier = Modifier.size(11.dp)
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(t.item, color = HistDeep, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text(t.person, color = HistMuted, fontSize = 11.sp, maxLines = 1)
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = if (isIn) "+${t.qty}" else "−${-t.qty}",
                color = accent,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold
            )
            val clock = clockOf(t.time)
            if (clock.isNotEmpty()) {
                Text(clock, color = HistLight, fontSize = 11.sp)
            }
        }
    }
}