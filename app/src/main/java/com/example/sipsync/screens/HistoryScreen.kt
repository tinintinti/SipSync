package com.example.sipsync.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HistoryScreen(isOwner: Boolean) {
    var filter by remember { mutableStateOf(0) } // 0 = All, 1 = Stock In, 2 = Stock Out

    val all = transactionsFor(isOwner)
    val shown = all.filter {
        filter == 0 || (filter == 1 && it.qty > 0) || (filter == 2 && it.qty < 0)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            kicker = if (isOwner) "ALL DEVICE ACTIVITY" else "YOUR ACTIVITY",
            title = "Transaction History"
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SipDeepTeal)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusDot(SipMintBright, 8.dp)
                Spacer(Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        if (isOwner) "Both authorized devices" else "Employee device",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text("${all.size} local records shown", color = SipMintBright, fontSize = 11.sp)
                }
                Icon(Icons.Outlined.Receipt, null, tint = SipTeal, modifier = Modifier.size(36.dp))
            }

            // Filter chips + date dropdown (dropdown is visual only for now)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PillChip("All", filter == 0, { filter = 0 })
                PillChip("Stock In", filter == 1, { filter = 1 })
                PillChip("Stock Out", filter == 2, { filter = 2 })
                Spacer(Modifier.weight(1f))
                val shape = RoundedCornerShape(12.dp)
                Row(
                    modifier = Modifier
                        .clip(shape)
                        .background(Color.White)
                        .border(1.dp, SipBorder, shape)
                        .padding(start = 12.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("All dates", color = SipTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = SipMuted, modifier = Modifier.size(18.dp))
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("ALL DATES", color = SipLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Text("${shown.size} RECORDS", color = SipLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
            }

            shown.forEach { TransactionRow(it) }
            Spacer(Modifier.height(8.dp))
        }
    }
}