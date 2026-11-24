package com.example.sipsync.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val FormGreen = Color(0xFF1E8E5A)
private val FormGreenBg = Color(0xFFDDF2E8)
private val FormRed = Color(0xFFD64545)
private val FormRedBg = Color(0xFFFFE0E6)

// quantity is always positive here. The caller turns it into +qty (stock in) or -qty (stock out).
@Composable
fun StockFormScreen(
    isStockIn: Boolean,
    beverages: List<Beverage>,
    onBack: () -> Unit,
    onSave: (beverage: Beverage, quantity: Int, note: String) -> Unit
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)
    DarkStatusBarIcons()

    var selected by remember { mutableStateOf<Beverage?>(null) }
    var qtyText by remember { mutableStateOf("1") }
    var note by remember { mutableStateOf("") }

    val accent = if (isStockIn) FormGreen else FormRed
    val accentBg = if (isStockIn) FormGreenBg else FormRedBg
    val qty = qtyText.toIntOrNull() ?: 0
    val current = selected?.units
    val after = current?.let { if (isStockIn) it + qty else it - qty }
    val notEnough = !isStockIn && after != null && after < 0

    fun submit() {
        val b = selected
        val error: String? = when {
            b == null -> "Please choose a beverage"
            qty < 1 -> "Enter a quantity of at least 1"
            !isStockIn && qty > b.units -> "Only ${b.units} units in stock"
            else -> null
        }
        if (error != null || b == null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            return
        }
        onSave(b, qty, note.trim())
        Toast.makeText(
            context,
            if (isStockIn) "Added $qty × ${b.name}" else "Removed $qty × ${b.name}",
            Toast.LENGTH_SHORT
        ).show()
    }

    Box(modifier = Modifier.fillMaxSize().background(AuthBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp, vertical = 16.dp)
        ) {
            // ===== Header =====
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "INVENTORY", color = AuthRaspberry, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                    )
                    Text(
                        if (isStockIn) "Stock In" else "Stock Out",
                        color = AuthPlum, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(accentBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (isStockIn) Icons.Outlined.Download else Icons.Outlined.Upload,
                        null, tint = accent, modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))
            Text(
                if (isStockIn) "Receive items and add them to your stock."
                else "Release items and remove them from your stock.",
                color = AuthMuted, fontSize = 12.sp,
                modifier = Modifier.padding(start = 56.dp)
            )

            Spacer(Modifier.height(18.dp))

            AuthCard {
                // ----- Beverage picker -----
                AuthLabel("Beverage")
                if (beverages.isEmpty()) {
                    Text(
                        "No beverages yet. The Owner needs to add one from Inventory first.",
                        color = AuthMuted, fontSize = 12.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        beverages.forEach { b ->
                            BeverageOption(b, selected = b == selected) { selected = b }
                        }
                    }
                }

                // ----- Quantity -----
                Spacer(Modifier.height(18.dp))
                AuthLabel("Quantity")
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StepButton(Icons.Filled.Remove, "Decrease") {
                        qtyText = maxOf(1, qty - 1).toString()
                    }
                    val shape = RoundedCornerShape(16.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(shape)
                            .background(AuthFieldBg)
                            .border(1.dp, AuthFieldBorder, shape),
                        contentAlignment = Alignment.Center
                    ) {
                        BasicTextField(
                            value = qtyText,
                            onValueChange = { new ->
                                if (new.length <= 6 && new.all { it.isDigit() }) qtyText = new
                            },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = AuthPlum, fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center
                            ),
                            cursorBrush = SolidColor(AuthRaspberry),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    StepButton(Icons.Filled.Add, "Increase") {
                        qtyText = (qty + 1).toString()
                    }
                }

                // ----- Preview -----
                if (selected != null && current != null && after != null) {
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (notEnough) FormRedBg else accentBg)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (notEnough) "Not enough stock" else "Stock after",
                            color = if (notEnough) FormRed else AuthMuted,
                            fontSize = 12.sp, modifier = Modifier.weight(1f)
                        )
                        Text(
                            if (notEnough) "$current available" else "$current → $after units",
                            color = if (notEnough) FormRed else accent,
                            fontSize = 13.sp, fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // ----- Note -----
                Spacer(Modifier.height(16.dp))
                AuthLabel("Note (optional)")
                AuthField(
                    note, { note = it },
                    if (isStockIn) "e.g. Supplier delivery" else "e.g. Sold at the counter",
                    Icons.Outlined.Edit
                )

                Spacer(Modifier.height(20.dp))
                AuthPrimaryButton(
                    text = if (isStockIn) "Confirm stock in" else "Confirm stock out",
                    onClick = { submit() }
                )

                Spacer(Modifier.height(10.dp))
                Text(
                    "Cancel",
                    color = AuthRaspberry,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(role = Role.Button, onClick = onBack)
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BeverageOption(b: Beverage, selected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) AuthFieldBg else Color.White)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) AuthRaspberry else AuthFieldBorder,
                shape = shape
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(b.bg),
            contentAlignment = Alignment.Center
        ) {
            Text(b.initials, color = b.tint, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(b.name, color = AuthPlum, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(b.category, color = AuthMuted, fontSize = 11.sp)
        }
        Text(
            if (b.units == 0) "Out of stock" else "${b.units} units",
            color = if (b.units == 0) FormRed else AuthMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StepButton(icon: ImageVector, description: String, onClick: () -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(shape)
            .background(Color.White)
            .border(1.dp, AuthFieldBorder, shape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, description, tint = AuthPlum)
    }
}