package com.example.sipsync.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

// ---------- Shared colors ----------
internal val SipDeepTeal = Color(0xFF073B3A)
internal val SipTeal = Color(0xFF087F70)
internal val SipCream = Color(0xFFF8F7F2)
internal val SipMint = Color(0xFFE7F4EF)
internal val SipMintBright = Color(0xFF5FE3C0)
internal val SipMintSoft = Color(0xFFB9DDD3)
internal val SipMuted = Color(0xFF6B7C79)
internal val SipLight = Color(0xFF9AA5A2)
internal val SipBorder = Color(0xFFE8ECEA)
internal val SipRed = Color(0xFFD64545)
internal val SipRedBg = Color(0xFFFDEAEA)
internal val SipAmber = Color(0xFFE0922B)
internal val SipAmberBg = Color(0xFFFFF1DC)
internal val SipBlue = Color(0xFF5B6BD6)
internal val SipBlueBg = Color(0xFFE9ECFB)

// ---------- Sample data (in memory only, no database) ----------
data class Beverage(
    val id: Int,
    val name: String,
    val category: String,
    val price: Double,
    val units: Int,
    val initials: String,
    val tint: Color,
    val bg: Color,
    val minStock: Int = 10
)

data class Txn(val item: String, val person: String, val time: String, val qty: Int)

val sampleBeverages = listOf(
    Beverage(1, "PureSpring Water", "Bottled Water", 20.0, 48, "PW", SipBlue, SipBlueBg),
    Beverage(2, "Cola Classic", "Soft Drinks", 35.0, 8, "CC", SipRed, SipRedBg),
    Beverage(3, "Sunny Orange Juice", "Juice", 42.0, 0, "OJ", SipAmber, SipAmberBg),
    Beverage(4, "Leaf Brew Tea", "Bottled Tea", 38.0, 24, "LT", SipTeal, SipMint),
    Beverage(5, "Daily Kick Coffee", "Canned Coffee", 45.0, 11, "DK", SipBlue, SipBlueBg)
)

val sampleTransactions = listOf(
    Txn("PureSpring Water", "Mara Santos · Owner", "Today, 9:42 AM", 24),
    Txn("Cola Classic", "Nico Reyes · Employee", "Today, 8:15 AM", -6),
    Txn("Leaf Brew Tea", "Nico Reyes · Employee", "Yesterday, 4:30 PM", 12),
    Txn("Sunny Orange Juice", "Mara Santos · Owner", "Yesterday, 2:18 PM", -4),
    Txn("Daily Kick Coffee", "Nico Reyes · Employee", "Jun 12, 11:06 AM", -3)
)

// Owner sees all records, Employee sees only their own
internal fun transactionsFor(isOwner: Boolean): List<Txn> =
    if (isOwner) sampleTransactions else sampleTransactions.filter { it.person.contains("Employee") }

// ---------- Main screen: content + bottom navigation ----------
@Composable
fun MainScreen(
    isOwner: Boolean,
    onSwitchRole: () -> Unit,
    onLogout: () -> Unit
) {
    var tab by remember { mutableStateOf(0) }
    var showAdd by remember { mutableStateOf(false) }
    val beverages = remember { mutableStateListOf<Beverage>().apply { addAll(sampleBeverages) } }

    // Home has a dark header (light status icons); other screens have white headers (dark icons)
    val view = LocalView.current
    SideEffect {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = tab != 0 || showAdd
    }
    DisposableEffect(Unit) {
        onDispose {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // Phone back button closes the Add Beverage screen
    BackHandler(enabled = showAdd) { showAdd = false }

    if (showAdd && isOwner) {
        AddBeverageScreen(
            nextId = (beverages.maxOfOrNull { it.id } ?: 0) + 1,
            onCancel = { showAdd = false },
            onSave = { newBeverage ->
                beverages.add(newBeverage)
                showAdd = false
                tab = 1
            }
        )
    } else {
        Column(modifier = Modifier.fillMaxSize().background(SipCream)) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                when (tab) {
                    0 -> HomeScreen(beverages = beverages, isOwner = isOwner, onNavigate = { tab = it })
                    1 -> InventoryScreen(
                        beverages = beverages,
                        isOwner = isOwner,
                        onAddClick = { showAdd = true }
                    )
                    2 -> HistoryScreen(isOwner = isOwner)
                    3 -> SyncScreen(isOwner = isOwner)
                    else -> ProfileScreen(
                        isOwner = isOwner,
                        onSwitchRole = onSwitchRole,
                        onLogout = onLogout
                    )
                }
            }
            BottomNavBar(selected = tab, onSelect = { tab = it })
        }
    }
}

@Composable
private fun BottomNavBar(selected: Int, onSelect: (Int) -> Unit) {
    val items = listOf(
        "Home" to Icons.Outlined.GridView,
        "Inventory" to Icons.Outlined.Inventory2,
        "History" to Icons.Outlined.Receipt,
        "Sync" to Icons.Outlined.Sync,
        "Profile" to Icons.Outlined.Person
    )
    Surface(color = Color.White, shadowElevation = 12.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 8.dp)
        ) {
            items.forEachIndexed { index, (label, icon) ->
                val active = index == selected
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(index) }
                        .padding(vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (active) SipTeal else SipLight,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                        color = if (active) SipTeal else SipLight
                    )
                }
            }
        }
    }
}

// ---------- Shared building blocks ----------
@Composable
internal fun SipCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = modifier
            .clip(shape)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = shape,
        color = Color.White,
        border = BorderStroke(1.dp, SipBorder)
    ) { content() }
}

@Composable
internal fun IconTile(icon: ImageVector, tint: Color, bg: Color, size: Dp = 40.dp) {
    Box(
        modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)).background(bg),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}

@Composable
internal fun Kicker(text: String) {
    Text(
        text = text,
        color = SipTeal,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp
    )
}

@Composable
internal fun SectionHeader(
    kicker: String,
    title: String,
    action: String? = null,
    onAction: () -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Kicker(kicker)
            Spacer(Modifier.height(4.dp))
            Text(title, color = SipDeepTeal, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
        if (action != null) {
            Text(
                text = action,
                color = SipTeal,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(onClick = onAction).padding(4.dp)
            )
        }
    }
}

@Composable
internal fun StatusDot(color: Color, size: Dp = 8.dp, modifier: Modifier = Modifier) {
    Box(modifier = modifier.size(size).clip(CircleShape).background(color))
}

@Composable
internal fun Chevron() {
    Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = SipLight,
        modifier = Modifier.size(20.dp)
    )
}

// The SipSync logo mark: bottle outline + small sync badge.
// Drawn on a 66-unit grid and scaled to any size.
@Composable
internal fun SipLogoMark(
    logoSize: Dp,
    bottleColor: Color = SipDeepTeal,
    badgeColor: Color = SipTeal
) {
    val u = logoSize / 66f
    Box(modifier = Modifier.size(logoSize)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val unit = this.size.width / 66f
            drawRoundRect(
                color = bottleColor,
                topLeft = Offset(27 * unit, 11 * unit),
                size = Size(9 * unit, 8 * unit),
                cornerRadius = CornerRadius(2 * unit)
            )
            drawRoundRect(
                color = bottleColor,
                topLeft = Offset(22 * unit, 21 * unit),
                size = Size(20 * unit, 32 * unit),
                cornerRadius = CornerRadius(8 * unit),
                style = Stroke(width = 3 * unit)
            )
        }
        Box(
            modifier = Modifier
                .offset(x = u * 33f, y = u * 42f)
                .size(width = u * 26f, height = u * 16f)
                .clip(RoundedCornerShape(u * 8f))
                .background(badgeColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SwapHoriz,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(u * 12f)
            )
        }
    }
}

// Small logo used in screen headers
@Composable
internal fun MiniLogo(modifier: Modifier = Modifier, onDark: Boolean = false) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (onDark) Color.White.copy(alpha = 0.12f) else Color.Transparent),
        contentAlignment = Alignment.Center
    ) {
        SipLogoMark(
            logoSize = 36.dp,
            bottleColor = if (onDark) SipMintBright else SipDeepTeal
        )
    }
}

@Composable
internal fun Avatar(initials: String, size: Dp) {
    Box(modifier = Modifier.size(size)) {
        Box(
            modifier = Modifier.fillMaxSize().clip(CircleShape).background(Color(0xFFD9A066)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = Color(0xFF5A2E14),
                fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.32f).sp
            )
        }
        StatusDot(
            color = Color(0xFF2ECC8F),
            size = size * 0.22f,
            modifier = Modifier.align(Alignment.BottomEnd)
        )
    }
}

@Composable
internal fun ScreenHeader(
    kicker: String,
    title: String,
    trailing: @Composable BoxScope.() -> Unit = {}
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        MiniLogo(Modifier.align(Alignment.CenterStart))
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Kicker(kicker)
            Text(title, color = SipDeepTeal, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        }
        trailing()
    }
}

@Composable
internal fun PillChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    activeColor: Color = SipTeal
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) activeColor else Color.White)
            .border(1.dp, if (selected) activeColor else SipBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White else SipMuted
        )
    }
}

@Composable
internal fun TransactionRow(t: Txn) {
    val isIn = t.qty > 0
    SipCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconTile(
                icon = if (isIn) Icons.Outlined.Download else Icons.Outlined.Upload,
                tint = if (isIn) SipTeal else SipRed,
                bg = if (isIn) SipMint else SipRedBg,
                size = 42.dp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(t.item, color = SipDeepTeal, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(t.person, color = Color(0xFFB5835A), fontSize = 11.sp)
                Spacer(Modifier.height(4.dp))
                Text(t.time, color = SipLight, fontSize = 10.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (isIn) "+${t.qty}" else "−${-t.qty}",
                    color = if (isIn) SipTeal else SipRed,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Text(if (isIn) "Stock In" else "Stock Out", color = SipMuted, fontSize = 10.sp)
            }
        }
    }
}