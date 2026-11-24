package com.example.sipsync.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---- Palette for this screen (matches the pink/peach design) ----
private val InvPink = Color(0xFFC2366B)
private val InvPinkSoft = Color(0xFFFBE3EB)
private val InvDeep = Color(0xFF5B1A33)
private val InvMuted = Color(0xFF7D6470)
private val InvLight = Color(0xFFA38C96)
private val InvBorder = Color(0xFFF3DDE3)

private val InvGreen = Color(0xFF1E7A57)
private val InvGreenBg = Color(0xFFDDF3E7)
private val InvAmber = Color(0xFF8A6414)
private val InvAmberBg = Color(0xFFFBEBC2)
private val InvRed = Color(0xFFC0392B)
private val InvRedBg = Color(0xFFFCDADA)

private val InvBackground = Brush.verticalGradient(
    listOf(Color(0xFFFFC9B0), Color(0xFFFDE0D6), Color(0xFFFEF4F0))
)

@Composable
fun InventoryScreen(
    beverages: List<Beverage>,
    isOwner: Boolean,
    onAddClick: () -> Unit,
    onDelete: (Beverage) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("All") }

    // Default categories plus any custom category the owner created
    val categories = listOf("All") + (DefaultBeverageCategories + beverages.map { it.category }).distinct()

    val shown = beverages
        .filter { category == "All" || it.category == category }
        .filter { it.name.contains(query, ignoreCase = true) }
        .sortedBy { it.name }

    Box(modifier = Modifier.fillMaxSize().background(InvBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 4.dp)
            ) {
                Text(
                    "${beverages.size} REGISTERED ITEMS",
                    color = InvPink,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp
                )
                Spacer(Modifier.height(4.dp))
                Text("Beverage Inventory", color = InvDeep, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total units + access pill
                val pillShape = RoundedCornerShape(50)
                Row(
                    modifier = Modifier
                        .shadow(2.dp, pillShape)
                        .clip(pillShape)
                        .background(Color.White)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Inventory2, null, tint = InvPink, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${beverages.sumOf { it.units }}", color = InvDeep, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                    Text(
                        " Total Units  ·  " + if (isOwner) "Management Access" else "View-Only Access",
                        color = InvMuted,
                        fontSize = 12.sp
                    )
                }

                // Search bar with the reset-filters button inside
                val searchShape = RoundedCornerShape(18.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(3.dp, searchShape)
                        .clip(searchShape)
                        .background(Color.White)
                        .padding(start = 14.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Search, null, tint = InvMuted, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (query.isEmpty()) Text("Search Beverages", color = InvMuted, fontSize = 14.sp)
                        BasicTextField(
                            value = query,
                            onValueChange = { query = it },
                            singleLine = true,
                            textStyle = TextStyle(color = InvDeep, fontSize = 14.sp),
                            cursorBrush = SolidColor(InvPink),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(InvPinkSoft)
                            .clickable { category = "All"; query = "" },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.Tune, "Reset filters", tint = InvPink, modifier = Modifier.size(20.dp))
                    }
                }

                // Category chips
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { c ->
                        CategoryChip(c, selected = category == c, onClick = { category = c })
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${shown.size} BEVERAGES", color = InvMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                    Text("SORTED A–Z", color = InvMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                }

                if (shown.isEmpty()) {
                    Text(
                        "No beverages found",
                        color = InvMuted,
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
                        onDelete = { if (isOwner) onDelete(b) }
                    )
                }
                Spacer(Modifier.height(if (isOwner) 80.dp else 16.dp)) // room for the floating button
            }
        }

        // Floating "Add Beverage" button (Owner only)
        if (isOwner) {
            val pill = RoundedCornerShape(50)
            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .shadow(8.dp, pill)
                    .clip(pill)
                    .background(InvPink)
                    .clickable(onClick = onAddClick)
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Add Beverage", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) InvPink else Color.White)
            .then(if (selected) Modifier else Modifier.border(1.dp, InvBorder, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            color = if (selected) Color.White else InvDeep,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun BeverageCard(
    beverage: Beverage,
    isOwner: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val outOfStock = beverage.units == 0
    val (label, labelColor, labelBg) = when {
        outOfStock -> Triple("Out of Stock", InvRed, InvRedBg)
        beverage.units <= beverage.minStock -> Triple("Low on Stock", InvAmber, InvAmberBg)
        else -> Triple("In Stock", InvGreen, InvGreenBg)
    }
    val priceText = "%.2f".format(beverage.price)
    val cardShape = RoundedCornerShape(20.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, cardShape)
            .clip(cardShape)
            .background(Color.White)
            .padding(start = 14.dp, top = 14.dp, bottom = 14.dp, end = if (isOwner) 4.dp else 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
            Text(beverage.name, color = InvDeep, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(Modifier.height(2.dp))
            Text("${beverage.category} · ₱$priceText", color = InvMuted, fontSize = 11.sp)
        }
        Spacer(Modifier.width(8.dp))

        // Units + status. When out of stock we hide the "0" and show only the status pill.
        Column(horizontalAlignment = Alignment.End) {
            if (!outOfStock) {
                Text("${beverage.units}", color = InvDeep, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(4.dp))
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(labelBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(label, color = labelColor, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Three-dot menu: owner only (employees get no edit/delete at all)
        if (isOwner) {
            var menuOpen by remember { mutableStateOf(false) }
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.MoreVert, "More options", tint = InvMuted, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        leadingIcon = { Icon(Icons.Outlined.Edit, null, tint = InvPink) },
                        onClick = { menuOpen = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = InvRed) },
                        leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = InvRed) },
                        onClick = { menuOpen = false; onDelete() }
                    )
                }
            }
        }
    }
}