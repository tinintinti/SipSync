package com.example.sipsync.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val categories = listOf("All", "Bottled Water", "Soft Drinks", "Juice", "Bottled Tea", "Canned Coffee")

@Composable
fun InventoryScreen(
    beverages: MutableList<Beverage>,
    isOwner: Boolean,
    onAddClick: () -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    val shown = beverages
        .filter { category == "All" || it.category == category }
        .filter { it.name.contains(query, ignoreCase = true) }
        .sortedBy { it.name }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader("${beverages.size} REGISTERED ITEMS", "Beverage Inventory") {
                if (isOwner) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SipTeal)
                            .clickable(onClick = onAddClick),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Add, "Add", tint = Color.White)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total units banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(SipDeepTeal)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Inventory2, null, tint = SipMintBright, modifier = Modifier.size(22.dp))
                    Spacer(Modifier.width(10.dp))
                    Text("${beverages.sumOf { it.units }}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    Spacer(Modifier.width(6.dp))
                    Text("Total units", color = SipMintSoft, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    StatusDot(SipMintBright, 6.dp)
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isOwner) "Management access" else "View-only access",
                        color = SipMintSoft,
                        fontSize = 10.sp
                    )
                }

                // Search + filter button
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    val shape = RoundedCornerShape(16.dp)
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = SipDeepTeal, fontSize = 14.sp),
                        cursorBrush = SolidColor(SipTeal),
                        modifier = Modifier.weight(1f).height(50.dp),
                        decorationBox = { inner ->
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(shape)
                                    .background(Color.White)
                                    .border(1.dp, SipBorder, shape)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Outlined.Search, null, tint = SipLight, modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(10.dp))
                                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                    if (query.isEmpty()) Text("Search beverages", color = SipLight, fontSize = 14.sp)
                                    inner()
                                }
                            }
                        }
                    )
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(shape)
                            .background(SipTeal)
                            .clickable { category = "All"; query = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Tune, "Reset filters", tint = Color.White)
                    }
                }

                // Category chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { c ->
                        PillChip(c, selected = category == c, onClick = { category = c }, activeColor = SipDeepTeal)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${shown.size} BEVERAGES", color = SipLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Text("SORTED A–Z", color = SipLight, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }

                if (shown.isEmpty()) {
                    Text(
                        "No beverages found",
                        color = SipMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        textAlign = TextAlign.Center
                    )
                }

                shown.forEach { b ->
                    BeverageCard(
                        beverage = b,
                        isOwner = isOwner,
                        onEdit = { Toast.makeText(context, "Edit form comes next", Toast.LENGTH_SHORT).show() },
                        onDelete = { beverages.remove(b) }
                    )
                }
                Spacer(Modifier.height(if (isOwner) 72.dp else 16.dp)) // room for the floating button
            }
        }

        // Floating "Add beverage" button (Owner only)
        if (isOwner) {
            val pill = RoundedCornerShape(50)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .shadow(8.dp, pill)
                    .clip(pill)
                    .background(SipTeal)
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 18.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add beverage", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BeverageCard(
    beverage: Beverage,
    isOwner: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val (label, labelColor, labelBg) = when {
        beverage.units == 0 -> Triple("OUT OF STOCK", SipRed, SipRedBg)
        beverage.units <= beverage.minStock -> Triple("LOW STOCK", SipAmber, SipAmberBg)
        else -> Triple("IN STOCK", SipTeal, SipMint)
    }
    val priceText = "%.2f".format(beverage.price)

    SipCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(beverage.bg),
                contentAlignment = Alignment.Center
            ) {
                Text(beverage.initials, color = beverage.tint, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(beverage.name, color = SipDeepTeal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text("${beverage.category} · ₱$priceText", color = SipMuted, fontSize = 11.sp)
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(labelBg)
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Text(label, color = labelColor, fontSize = 8.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("${beverage.units}", color = SipDeepTeal, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Text("units", color = SipLight, fontSize = 10.sp)
                if (isOwner) {
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        SmallAction(Icons.Outlined.Edit, SipTeal, SipMint, onEdit)
                        SmallAction(Icons.Outlined.Delete, SipRed, SipRedBg, onDelete)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmallAction(
    icon: ImageVector,
    tint: Color,
    bg: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
    }
}