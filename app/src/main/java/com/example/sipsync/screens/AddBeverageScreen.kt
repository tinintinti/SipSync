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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val categoryOptions = listOf("Bottled Water", "Soft Drinks", "Juice", "Bottled Tea", "Canned Coffee")

// Color pairs used for the new beverage's initials tile
private val tilePalette = listOf(
    SipBlue to SipBlueBg,
    SipRed to SipRedBg,
    SipAmber to SipAmberBg,
    SipTeal to SipMint
)

@Composable
fun AddBeverageScreen(
    nextId: Int,
    onCancel: () -> Unit,
    onSave: (Beverage) -> Unit
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categoryOptions[0]) }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }
    var menuOpen by remember { mutableStateOf(false) }

    fun save() {
        val priceValue = price.toDoubleOrNull()
        val stockValue = if (stock.isBlank()) 0 else stock.toIntOrNull()
        val minValue = if (minStock.isBlank()) 10 else minStock.toIntOrNull()

        val error = when {
            name.isBlank() -> "Please enter the beverage name"
            priceValue == null -> "Please enter a valid price"
            stockValue == null || minValue == null -> "Please enter valid numbers"
            else -> null
        }
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            return
        }

        val words = name.trim().split(" ").filter { it.isNotBlank() }
        val initials = words.take(2).joinToString("") { it.first().uppercase() }
        val (tint, bg) = tilePalette[nextId % tilePalette.size]

        onSave(
            Beverage(
                id = nextId,
                name = name.trim(),
                category = category,
                price = priceValue!!,
                units = stockValue!!,
                initials = initials,
                tint = tint,
                bg = bg,
                minStock = minValue!!
            )
        )
        Toast.makeText(context, "Beverage added", Toast.LENGTH_SHORT).show()
    }

    Column(modifier = Modifier.fillMaxSize().background(SipCream)) {

        // ===== Header =====
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            val backShape = RoundedCornerShape(12.dp)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(40.dp)
                    .clip(backShape)
                    .background(SipMint)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back", tint = SipDeepTeal)
            }
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Kicker("OWNER MANAGEMENT")
                Text("Add Beverage", color = SipDeepTeal, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        // ===== Form =====
        Column(
            modifier = Modifier
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconTile(Icons.Default.Add, SipTeal, SipMint, 48.dp)
                Spacer(Modifier.width(14.dp))
                Column {
                    Text("Register a new beverage", color = SipDeepTeal, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                    Text("Keep product information clear and accurate.", color = SipMuted, fontSize = 11.sp)
                }
            }

            Spacer(Modifier.height(20.dp))

            FormLabel("Beverage name")
            FormField(name, { name = it }, "e.g. PureSpring Water", KeyboardType.Text)

            Spacer(Modifier.height(16.dp))

            FormLabel("Category")
            Box {
                val shape = RoundedCornerShape(16.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .clip(shape)
                        .background(Color.White)
                        .border(1.dp, SipBorder, shape)
                        .clickable { menuOpen = true }
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(category, color = SipDeepTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.KeyboardArrowDown, null, tint = SipDeepTeal, modifier = Modifier.size(20.dp))
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    categoryOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option, fontSize = 13.sp) },
                            onClick = {
                                category = option
                                menuOpen = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Price (₱)")
                    FormField(
                        value = price,
                        onValueChange = { new ->
                            // digits and at most one dot
                            if (new.all { it.isDigit() || it == '.' } && new.count { it == '.' } <= 1) price = new
                        },
                        placeholder = "0.00",
                        keyboardType = KeyboardType.Decimal
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    FormLabel("Initial stock")
                    FormField(
                        value = stock,
                        onValueChange = { new -> if (new.all { it.isDigit() }) stock = new },
                        placeholder = "0",
                        keyboardType = KeyboardType.Number
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            FormLabel("Minimum stock level")
            FormField(
                value = minStock,
                onValueChange = { new -> if (new.all { it.isDigit() }) minStock = new },
                placeholder = "Set reorder threshold",
                keyboardType = KeyboardType.Number
            )

            Spacer(Modifier.height(16.dp))

            // Info box
            val infoShape = RoundedCornerShape(14.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(infoShape)
                    .background(SipMint)
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Info, null, tint = SipTeal, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text(
                    "Low-stock alerts appear when available quantity reaches the minimum level.",
                    color = SipTeal,
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // Buttons
            Row(
                modifier = Modifier.navigationBarsPadding(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val btnShape = RoundedCornerShape(16.dp)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(btnShape)
                        .background(Color.White)
                        .border(1.dp, SipBorder, btnShape)
                        .clickable(onClick = onCancel),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Cancel", color = SipMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Row(
                    modifier = Modifier
                        .weight(1.6f)
                        .height(52.dp)
                        .shadow(8.dp, btnShape, ambientColor = SipTeal, spotColor = SipTeal)
                        .clip(btnShape)
                        .background(SipTeal)
                        .clickable { save() },
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Add beverage", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        color = SipDeepTeal,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = SipDeepTeal, fontSize = 13.sp, fontWeight = FontWeight.Bold),
        cursorBrush = SolidColor(SipTeal),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(Color.White)
                    .border(
                        width = if (focused) 1.5.dp else 1.dp,
                        color = if (focused) SipTeal else SipBorder,
                        shape = shape
                    )
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(placeholder, color = SipLight, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                }
                inner()
            }
        }
    )
}