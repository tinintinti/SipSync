package com.example.sipsync.screens

import androidx.compose.ui.graphics.Color
import com.example.sipsync.data.BeverageWithStock
import com.example.sipsync.data.TransactionEntity
import com.example.sipsync.data.UserEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val tiles = listOf(
    SipBlue to SipBlueBg,
    SipRed to SipRedBg,
    SipAmber to SipAmberBg,
    SipTeal to SipMint
)

// Database row -> the model the screens show
internal fun BeverageWithStock.toUi(index: Int): Beverage {
    val (tint: Color, bg: Color) = tiles[index % tiles.size]
    val initials = name.trim().split(" ").filter { it.isNotBlank() }
        .take(2).joinToString("") { it.first().uppercase() }
        .ifEmpty { "?" }
    return Beverage(
        id = index + 1,
        name = name,
        category = category,
        price = price,
        units = units,
        initials = initials,
        tint = tint,
        bg = bg,
        minStock = minStock,
        key = id // the real database id (UUID)
    )
}

// Transaction rows -> the model History and Home show
internal fun buildTxns(
    rows: List<TransactionEntity>,
    beverages: List<Beverage>,
    me: UserEntity?
): List<Txn> {
    if (me == null) return emptyList()
    val myRole = if (me.role == "OWNER") "Owner" else "Employee"
    val otherRole = if (me.role == "OWNER") "Employee" else "Owner"
    return rows.map { t ->
        Txn(
            item = beverages.firstOrNull { it.key == t.beverageId }?.name ?: "Removed item",
            person = if (t.userId == me.id) "${me.fullName} · $myRole" else otherRole,
            time = formatTime(t.createdAt),
            qty = t.quantity
        )
    }
}

private fun sameDay(a: Calendar, b: Calendar) =
    a.get(Calendar.YEAR) == b.get(Calendar.YEAR) &&
        a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR)

internal fun formatTime(millis: Long): String {
    val then = Calendar.getInstance().apply { timeInMillis = millis }
    val today = Calendar.getInstance()
    val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
    val time = SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(millis))
    return when {
        sameDay(today, then) -> "Today, $time"
        sameDay(yesterday, then) -> "Yesterday, $time"
        else -> SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(millis))
    }
}