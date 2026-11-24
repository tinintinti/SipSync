package com.example.sipsync.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Sync
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import kotlinx.coroutines.delay

// ---------- Auth theme (login + register) ----------
internal val AuthPlum = Color(0xFF4B1633)
internal val AuthRaspberry = Color(0xFFB8335F)
internal val AuthRaspberryDark = Color(0xFFA32A55)
internal val AuthOrange = Color(0xFFF28C4B)
internal val AuthFieldBg = Color(0xFFFFF4EC)
internal val AuthFieldBorder = Color(0xFFF5DCD0)
internal val AuthMuted = Color(0xFF7D5C69)

// Full-page gradient
internal val AuthBackground = Brush.verticalGradient(
    listOf(Color(0xFFFFC7A8), Color(0xFFFFE2D4), Color(0xFFFFF5F0))
)

@Composable
fun LoginScreen(
    loginError: String? = null,
    storeExists: Boolean = false,
    onRegisterStore: () -> Unit = {},
    onGetAccount: () -> Unit = {},
    onLogin: (username: String, password: String, role: String) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    DarkStatusBarIcons()

    LaunchedEffect(loginError) {
        if (loginError != null) Toast.makeText(context, loginError, Toast.LENGTH_SHORT).show()
    }

    var role by remember { mutableStateOf("Owner") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmNewStore by remember { mutableStateOf(false) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(AuthBackground)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ===== Top: logo, name, card =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(30.dp))
                AuthLogo()
                Spacer(Modifier.height(14.dp))
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = AuthPlum)) { append("Sip") }
                        withStyle(SpanStyle(color = AuthRaspberry)) { append("Sync") }
                    },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Offline Beverage Inventory, Always in Sync.",
                    color = AuthPlum,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(24.dp))

                AuthCard {
                    Text(
                        "WELCOME BACK", color = AuthRaspberry, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Sign in to your Store", color = AuthPlum, fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.3).sp
                    )
                    Spacer(Modifier.height(4.dp))
                    Text("Use Your Authorized Device Account.", color = AuthMuted, fontSize = 13.sp)

                    Spacer(Modifier.height(18.dp))
                    AuthLabel("Sign in as")
                    RoleToggle(selected = role, onSelect = { role = it })

                    Spacer(Modifier.height(16.dp))
                    AuthLabel("Username")
                    AuthField(username, { username = it }, "Username", Icons.Outlined.Person)

                    Spacer(Modifier.height(16.dp))
                    AuthLabel("Password")
                    AuthField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = "Password",
                        leadingIcon = Icons.Outlined.Lock,
                        isPassword = true,
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible }
                    )

                    Spacer(Modifier.height(20.dp))
                    AuthPrimaryButton(
                        text = "Log in",
                        trailing = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        onClick = {
                            if (username.isBlank() || password.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter your username and password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onLogin(username.trim(), password, role)
                            }
                        }
                    )

                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            if (storeExists) "Different store? " else "New store? ",
                            color = AuthMuted,
                            fontSize = 13.sp
                        )
                        Text(
                            if (storeExists) "Register a new store" else "Register your store",
                            color = AuthRaspberry,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(role = Role.Button) {
                                    if (storeExists) confirmNewStore = true else onRegisterStore()
                                }
                                .padding(vertical = 8.dp)
                        )
                    }
                    // Employee phone: pull the account from the Owner's phone
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Employee? ", color = AuthMuted, fontSize = 13.sp)
                        Text(
                            "Get account from Owner phone",
                            color = AuthRaspberry,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier
                                .clickable(role = Role.Button, onClick = onGetAccount)
                                .padding(vertical = 8.dp)
                        )
                    }
                }
            }

            // ===== Bottom: offline pill + drinks illustration =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val pill = RoundedCornerShape(50)
                Row(
                    modifier = Modifier
                        .clip(pill)
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, AuthFieldBorder, pill)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Wifi, null, tint = AuthRaspberry, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        "Works Offline · Wi-Fi Direct Ready", color = AuthPlum,
                        fontSize = 11.sp, fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(18.dp))
                DrinksIllustration()
            }
        }
    }

    // Warning before replacing an existing store
    if (confirmNewStore) {
        AlertDialog(
            onDismissRequest = { confirmNewStore = false },
            title = { Text("Register a new store?") },
            text = {
                Text(
                    "This phone already has a store. Registering a new one erases the current " +
                        "store's beverages, history and accounts from this phone. This cannot be undone."
                )
            },
            confirmButton = {
                TextButton(onClick = { confirmNewStore = false; onRegisterStore() }) {
                    Text("Erase and continue", color = AuthRaspberry)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmNewStore = false }) { Text("Cancel") }
            }
        )
    }
}

// ---------- Role toggle ----------
@Composable
private fun RoleToggle(selected: String, onSelect: (String) -> Unit) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(AuthFieldBg)
            .border(1.dp, AuthFieldBorder, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        RoleOption(
            "Owner", "Full access", Icons.Outlined.Shield,
            selected == "Owner", { onSelect("Owner") }, Modifier.weight(1f)
        )
        RoleOption(
            "Employee", "Daily operations", Icons.Outlined.Inventory2,
            selected == "Employee", { onSelect("Employee") }, Modifier.weight(1f)
        )
    }
}

@Composable
private fun RoleOption(
    title: String,
    subtitle: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = modifier
            .shadow(if (selected) 6.dp else 0.dp, shape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
            .clip(shape)
            .then(
                if (selected) Modifier.background(Brush.horizontalGradient(listOf(AuthRaspberry, AuthRaspberryDark)))
                else Modifier
            )
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = if (selected) Color.White else AuthPlum, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                title, color = if (selected) Color.White else AuthPlum,
                fontSize = 13.sp, fontWeight = FontWeight.Bold
            )
            Text(
                subtitle, color = if (selected) Color.White.copy(alpha = 0.9f) else AuthMuted,
                fontSize = 10.sp, lineHeight = 12.sp
            )
        }
    }
}

// ---------- Shared auth pieces (also used by RegisterStoreScreen) ----------
@Composable
internal fun AuthCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(28.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
            .clip(shape)
            .background(Color.White)
            .padding(horizontal = 22.dp, vertical = 24.dp),
        content = content
    )
}

@Composable
internal fun AuthLabel(text: String) {
    Text(
        text = text,
        color = AuthPlum,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
internal fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    trailing: ImageVector? = null
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .shadow(10.dp, shape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
            .clip(shape)
            .background(Brush.horizontalGradient(listOf(AuthRaspberry, AuthRaspberryDark)))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 18.dp),
        horizontalArrangement = if (trailing != null) Arrangement.SpaceBetween else Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        if (trailing != null) {
            Icon(trailing, null, tint = Color.White, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
internal fun AuthField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(16.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = AuthPlum, fontSize = 14.sp, fontWeight = FontWeight.Bold),
        cursorBrush = SolidColor(AuthRaspberry),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation()
        else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(AuthFieldBg)
                    .border(
                        width = if (focused) 1.5.dp else 1.dp,
                        color = if (focused) AuthRaspberry else AuthFieldBorder,
                        shape = shape
                    )
                    .padding(start = 14.dp, end = if (isPassword) 6.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(leadingIcon, null, tint = AuthMuted, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) Text(placeholder, color = AuthMuted, fontSize = 14.sp)
                    inner()
                }
                if (isPassword) {
                    IconButton(onClick = onTogglePassword, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password",
                            tint = AuthMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    )
}

// Gradient app icon: white bottle with a sync arrow
@Composable
internal fun AuthLogo(logoSize: Dp = 84.dp) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .size(logoSize)
            .shadow(12.dp, shape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
            .clip(shape)
            .background(Brush.linearGradient(listOf(AuthOrange, AuthRaspberry))),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(logoSize * 0.5f)) {
            val w = size.width
            val h = size.height
            drawRoundRect(
                Color.White, Offset(w * 0.38f, 0f), Size(w * 0.24f, h * 0.14f), CornerRadius(w * 0.04f)
            )
            drawRoundRect(
                Color.White, Offset(w * 0.3f, h * 0.18f), Size(w * 0.4f, h * 0.82f), CornerRadius(w * 0.16f)
            )
        }
        Icon(
            Icons.Outlined.Sync, null, tint = AuthRaspberry,
            modifier = Modifier.size(logoSize * 0.22f).offset(y = logoSize * 0.07f)
        )
    }
}

// Row of simple drink shapes along the bottom of the screen
@Composable
internal fun DrinksIllustration() {
    Canvas(modifier = Modifier.fillMaxWidth().height(100.dp)) {
        fun px(v: Float) = v.dp.toPx()
        val w = size.width
        val b = size.height

        drawBottle(w * 0.16f, b, px(38f), px(74f), AuthRaspberry, Color.White)

        // can
        drawRoundRect(AuthOrange, Offset(w * 0.34f - px(15f), b - px(46f)), Size(px(30f), px(46f)), CornerRadius(px(7f)))
        drawRect(Color.White.copy(alpha = 0.9f), Offset(w * 0.34f - px(15f), b - px(30f)), Size(px(30f), px(10f)))

        // tall water bottle
        drawBottle(w * 0.5f, b, px(34f), px(98f), Color(0xFFFFC9DA), AuthRaspberry)

        // juice carton with straw
        drawRoundRect(AuthOrange, Offset(w * 0.67f - px(18f), b - px(66f)), Size(px(36f), px(66f)), CornerRadius(px(6f)))
        drawLine(AuthRaspberry, Offset(w * 0.67f + px(4f), b - px(66f)), Offset(w * 0.67f + px(10f), b - px(80f)), strokeWidth = px(3f))
        drawCircle(Color.White, px(8f), Offset(w * 0.67f, b - px(34f)))

        drawBottle(w * 0.84f, b, px(30f), px(76f), AuthPlum, AuthOrange)
    }
}

private fun DrawScope.drawBottle(
    cx: Float, bottom: Float, w: Float, h: Float, color: Color, label: Color
) {
    val bodyH = h * 0.62f
    val neckW = w * 0.42f
    // neck
    drawRoundRect(color, Offset(cx - neckW / 2, bottom - h * 0.92f), Size(neckW, h * 0.4f), CornerRadius(neckW * 0.3f))
    // body
    drawRoundRect(color, Offset(cx - w / 2, bottom - bodyH), Size(w, bodyH), CornerRadius(w * 0.3f))
    // cap
    drawRoundRect(color.copy(alpha = 0.8f), Offset(cx - neckW * 0.6f, bottom - h), Size(neckW * 1.2f, h * 0.1f), CornerRadius(neckW * 0.2f))
    // label
    drawCircle(label, w * 0.18f, Offset(cx, bottom - bodyH * 0.5f))
}

// Peach background means dark status bar icons. Re-applied after the screen fade.
@Composable
internal fun DarkStatusBarIcons() {
    val view = LocalView.current
    fun apply() {
        val window = (view.context as Activity).window
        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
    }
    SideEffect { apply() }
    LaunchedEffect(Unit) {
        delay(500)
        apply()
    }
}