package com.example.sipsync.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.EmojiFoodBeverage
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalBar
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.LocalDrink
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Built-in categories. InventoryScreen also uses this list (plus any custom ones found on beverages). */
val DefaultBeverageCategories = listOf("Bottled Water", "Soft Drinks", "Juice", "Bottled Tea", "Canned Coffee")

// ---- Palette for this screen (pink/peach design) ----
private val AddPink = Color(0xFFC2366B)
private val AddPinkSoft = Color(0xFFFBE3EB)
private val AddDeep = Color(0xFF5B1A33)
private val AddMuted = Color(0xFF7D6470)
private val AddLight = Color(0xFFA38C96)
private val AddBorder = Color(0xFFF3DDE3)
private val AddField = Color(0xFFFFF4EF)
private val AddFieldBorder = Color(0xFFF6DCD3)

private val AddBackground = Brush.verticalGradient(
    listOf(Color(0xFFFFC9B0), Color(0xFFFDE0D6), Color(0xFFFEF4F0))
)

// Color pairs (icon tint to tile background) used for the new beverage's tile
private val tilePalette = listOf(
    Color(0xFF2F7DC4) to Color(0xFFE3EFFB),
    Color(0xFFD94B4B) to Color(0xFFFCE3E3),
    Color(0xFFD9822B) to Color(0xFFFCEBD3),
    Color(0xFF2E9E6B) to Color(0xFFDDF3E7),
    Color(0xFF8B5CC6) to Color(0xFFEEE5F8)
)

private fun categoryIcon(name: String): ImageVector = when (name) {
    "Bottled Water" -> Icons.Outlined.WaterDrop
    "Soft Drinks" -> Icons.Outlined.LocalDrink
    "Juice" -> Icons.Outlined.LocalBar
    "Bottled Tea" -> Icons.Outlined.EmojiFoodBeverage
    "Canned Coffee" -> Icons.Outlined.LocalCafe
    else -> Icons.Outlined.Category // custom categories
}

@Composable
fun AddBeverageScreen(
    nextId: Int,
    onCancel: () -> Unit,
    onSave: (Beverage) -> Unit,
    // Pass beverages.map { it.category } so categories created earlier show up here again
    existingCategories: List<String> = emptyList()
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(DefaultBeverageCategories[0]) }
    var price by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var minStock by remember { mutableStateOf("") }

    var customCategories by remember { mutableStateOf(emptyList<String>()) }
    var showAddCategory by remember { mutableStateOf(false) }
    var newCategory by remember { mutableStateOf("") }

    val allCategories = (DefaultBeverageCategories + existingCategories + customCategories).distinct()
    val (tint, bg) = tilePalette[nextId % tilePalette.size]

    fun addCategory() {
        val clean = newCategory.trim().replace(Regex("\\s+"), " ")
        if (clean.isEmpty()) {
            Toast.makeText(context, "Please enter a category name", Toast.LENGTH_SHORT).show()
            return
        }
        val existing = allCategories.firstOrNull { it.equals(clean, ignoreCase = true) }
        if (existing != null) {
            category = existing
        } else {
            customCategories = customCategories + clean
            category = clean
        }
        newCategory = ""
        showAddCategory = false
    }

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

    val cardShape = RoundedCornerShape(24.dp)

    Column(modifier = Modifier.fillMaxSize().background(AddBackground)) {

        // ===== Header =====
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val backShape = RoundedCornerShape(14.dp)
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .shadow(2.dp, backShape)
                    .clip(backShape)
                    .background(Color.White)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Back", tint = AddDeep)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text("OWNER MANAGEMENT", color = AddPink, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Text("Add Beverage", color = AddDeep, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            }
        }

        // ===== Content =====
        Column(
            modifier = Modifier
                .weight(1f)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ----- Preview card -----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, cardShape)
                    .clip(cardShape)
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text("PREVIEW", color = AddPink, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(categoryIcon(category), null, tint = tint, modifier = Modifier.size(28.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            name.ifBlank { "Beverage Name" },
                            color = AddDeep,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "$category · ₱" + "%.2f".format(price.toDoubleOrNull() ?: 0.0),
                            color = AddMuted,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text("${stock.toIntOrNull() ?: 0}", color = AddDeep, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                        Spacer(Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(AddPinkSoft)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            Text("New", color = AddPink, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ----- Form card -----
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, cardShape)
                    .clip(cardShape)
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                FormLabel("Category")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allCategories.forEach { c ->
                        CategoryTile(
                            label = c,
                            icon = categoryIcon(c),
                            selected = category == c,
                            onClick = { category = c }
                        )
                    }
                    // Owner can create their own category
                    CategoryTile(
                        label = "New",
                        icon = Icons.Default.Add,
                        selected = false,
                        onClick = { showAddCategory = true }
                    )
                }

                Spacer(Modifier.height(16.dp))

                FormLabel("Beverage Name")
                FormField(name, { name = it }, "e.g. PureSpring Water", KeyboardType.Text)

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Price")
                        FormField(
                            value = price,
                            onValueChange = { new ->
                                // digits and at most one dot
                                if (new.all { it.isDigit() || it == '.' } && new.count { it == '.' } <= 1) price = new
                            },
                            placeholder = "0.00",
                            keyboardType = KeyboardType.Decimal,
                            prefix = "₱"
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Initial Stock")
                        FormField(
                            value = stock,
                            onValueChange = { new -> if (new.all { it.isDigit() }) stock = new },
                            placeholder = "0",
                            keyboardType = KeyboardType.Number,
                            centered = true
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        FormLabel("Min. Stock")
                        FormField(
                            value = minStock,
                            onValueChange = { new -> if (new.all { it.isDigit() }) minStock = new },
                            placeholder = "10",
                            keyboardType = KeyboardType.Number,
                            centered = true
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Info box
                val infoShape = RoundedCornerShape(14.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(infoShape)
                        .background(AddPinkSoft)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Info, null, tint = AddPink, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Low-stock alert appears when the quantity reaches the minimum level.",
                        color = AddMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // ===== Bottom action bar =====
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val btnShape = RoundedCornerShape(18.dp)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
                    .clip(btnShape)
                    .background(Color.White)
                    .border(1.dp, AddBorder, btnShape)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Text("Cancel", color = AddMuted, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
            Row(
                modifier = Modifier
                    .weight(1.6f)
                    .height(52.dp)
                    .shadow(8.dp, btnShape, ambientColor = AddPink, spotColor = AddPink)
                    .clip(btnShape)
                    .background(AddPink)
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

    // ===== "New category" dialog =====
    if (showAddCategory) {
        AlertDialog(
            onDismissRequest = { showAddCategory = false; newCategory = "" },
            title = { Text("New category", color = AddDeep, fontWeight = FontWeight.ExtraBold) },
            text = {
                OutlinedTextField(
                    value = newCategory,
                    onValueChange = { if (it.length <= 24) newCategory = it },
                    singleLine = true,
                    placeholder = { Text("e.g. Energy Drinks") }
                )
            },
            confirmButton = {
                TextButton(onClick = { addCategory() }) {
                    Text("Add", color = AddPink, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCategory = false; newCategory = "" }) {
                    Text("Cancel", color = AddMuted)
                }
            }
        )
    }
}

@Composable
private fun CategoryTile(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)
    val isNew = label == "New"
    Column(
        modifier = Modifier
            .width(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.padding(top = 6.dp, end = 6.dp)) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(shape)
                    .background(if (selected) AddPinkSoft else AddField)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) AddPink else AddFieldBorder,
                        shape = shape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    null,
                    tint = if (selected || isNew) AddPink else AddMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
            if (selected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 6.dp, y = (-6).dp)
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(AddPink),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(12.dp))
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            label,
            color = if (selected || isNew) AddPink else AddMuted,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            lineHeight = 12.sp,
            maxLines = 2
        )
    }
}

@Composable
private fun FormLabel(text: String) {
    Text(
        text = text,
        color = AddDeep,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    prefix: String? = null,
    centered: Boolean = false
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = AddDeep,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = if (centered) TextAlign.Center else TextAlign.Start
        ),
        cursorBrush = SolidColor(AddPink),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(AddField)
                    .border(
                        width = if (focused) 1.5.dp else 1.dp,
                        color = if (focused) AddPink else AddFieldBorder,
                        shape = shape
                    )
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (prefix != null) {
                    Text(prefix, color = AddMuted, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.width(8.dp))
                }
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(placeholder, color = AddLight, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                    inner()
                }
            }
        }
    )
}