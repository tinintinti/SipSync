package com.example.sipsync.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- Colors ----------
private val DeepTeal = Color(0xFF073B3A)
private val PrimaryTeal = Color(0xFF087F70)
private val Cream = Color(0xFFF8F7F2)
private val MintBright = Color(0xFF5FE3C0)
private val MintSoft = Color(0xFFCDEBE3)
private val FieldBorder = Color(0xFFE2E7E5)
private val FieldBackground = Color(0xFFF8F9F8)
private val MutedText = Color(0xFF6B7C79)
private val LightText = Color(0xFF9AA5A2)

@Composable
fun LoginScreen(
    loginError: String? = null,
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current

    // Show wrong-password message from the database check
    LaunchedEffect(loginError) {
        if (loginError != null) {
            Toast.makeText(context, loginError, Toast.LENGTH_SHORT).show()
        }
    }

    // Fields start empty: no hard-coded credentials
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // ===== Header + overlapping card =====
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .drawHeaderBackground()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(28.dp))
                    BrandingSection()
                    Spacer(Modifier.height(26.dp))

                    LoginCard(
                        username = username,
                        onUsernameChange = { username = it },
                        password = password,
                        onPasswordChange = { password = it },
                        passwordVisible = passwordVisible,
                        onTogglePassword = { passwordVisible = !passwordVisible },
                        onLoginClick = {
                            if (username.isBlank() || password.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter your username and password",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                onLogin(username.trim(), password)
                            }
                        }
                    )
                }
            }

            // ===== Footer =====
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 24.dp, bottom = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = null,
                    tint = LightText,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Works offline · Wi-Fi Direct ready",
                    color = LightText,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// ---------- Header background: slanted teal shape + decorative rings ----------
private fun Modifier.drawHeaderBackground(): Modifier = this.drawBehind {
    val leftHeight = 358.dp.toPx()
    val rightHeight = 290.dp.toPx()

    val shape = Path().apply {
        moveTo(0f, 0f)
        lineTo(size.width, 0f)
        lineTo(size.width, rightHeight)
        lineTo(0f, leftHeight)
        close()
    }
    drawPath(shape, DeepTeal)

    clipPath(shape) {
        val stroke = Stroke(width = 1.dp.toPx())
        val ring = Color.White.copy(alpha = 0.09f)
        drawCircle(ring, 115.dp.toPx(), Offset(size.width * 0.97f, 90.dp.toPx()), style = stroke)
        drawCircle(ring, 120.dp.toPx(), Offset(-20.dp.toPx(), 285.dp.toPx()), style = stroke)
    }
}

// ---------- Branding ----------
@Composable
private fun BrandingSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AppLogo()
        Spacer(Modifier.height(14.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = Color.White)) { append("Sip") }
                withStyle(SpanStyle(color = MintBright)) { append("Sync") }
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Offline beverage inventory, always in sync.",
            color = MintSoft,
            fontSize = 13.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AppLogo() {
    Box(
        modifier = Modifier
            .size(66.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        SipLogoMark(logoSize = 66.dp)
    }
}

// ---------- Login card ----------
@Composable
private fun LoginCard(
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePassword: () -> Unit,
    onLoginClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp)) {
            Text(
                text = "WELCOME BACK",
                color = PrimaryTeal,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Sign in to your store",
                color = DeepTeal,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.3).sp
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Use your authorized device account.",
                color = MutedText,
                fontSize = 13.sp
            )

            Spacer(Modifier.height(20.dp))

            FieldLabel("Username")
            InputField(
                value = username,
                onValueChange = onUsernameChange,
                placeholder = "Username",
                leadingIcon = Icons.Outlined.Person
            )

            Spacer(Modifier.height(16.dp))

            FieldLabel("Password")
            InputField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Password",
                leadingIcon = Icons.Outlined.Lock,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = onTogglePassword
            )

            Spacer(Modifier.height(20.dp))

            val buttonShape = RoundedCornerShape(16.dp)
            Button(
                onClick = onLoginClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .shadow(
                        elevation = 10.dp,
                        shape = buttonShape,
                        ambientColor = PrimaryTeal.copy(alpha = 0.4f),
                        spotColor = PrimaryTeal.copy(alpha = 0.5f)
                    ),
                shape = buttonShape,
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryTeal,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Log in", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = DeepTeal,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 6.dp)
    )
}

@Composable
private fun InputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: () -> Unit = {}
) {
    var focused by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(14.dp)

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(
            color = DeepTeal,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        ),
        cursorBrush = SolidColor(PrimaryTeal),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation()
        else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .onFocusChanged { focused = it.isFocused },
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(FieldBackground)
                    .border(
                        width = if (focused) 1.5.dp else 1.dp,
                        color = if (focused) PrimaryTeal else FieldBorder,
                        shape = shape
                    )
                    .padding(start = 14.dp, end = if (isPassword) 6.dp else 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = LightText,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(text = placeholder, color = LightText, fontSize = 14.sp)
                    }
                    innerTextField()
                }
                if (isPassword) {
                    IconButton(onClick = onTogglePassword, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff
                            else Icons.Outlined.Visibility,
                            contentDescription = if (passwordVisible) "Hide password"
                            else "Show password",
                            tint = LightText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    )
}