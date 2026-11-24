package com.example.sipsync.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// onCreate reports back through onResult: true = account created, false = username taken.
@Composable
fun AddEmployeeScreen(
    onBack: () -> Unit,
    onCreate: (
        fullName: String,
        username: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)
    DarkStatusBarIcons()

    var fullName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    fun submit() {
        val error: String? = when {
            fullName.isBlank() -> "Please enter the employee's full name"
            username.trim().length < 3 -> "Username must be at least 3 characters"
            password.length < 6 -> "Password must be at least 6 characters"
            password != confirm -> "Passwords do not match"
            else -> null
        }
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            return
        }
        onCreate(fullName.trim(), username.trim(), password) { ok ->
            if (ok) {
                Toast.makeText(context, "Employee account created", Toast.LENGTH_SHORT).show()
                onBack()
            } else {
                Toast.makeText(context, "That username is already taken", Toast.LENGTH_SHORT).show()
            }
        }
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
                Column {
                    Text(
                        "OWNER ONLY", color = AuthRaspberry, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                    )
                    Text(
                        "Add Employee", color = AuthPlum, fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            AuthCard {
                AuthLabel("Full name")
                AuthField(fullName, { fullName = it }, "e.g. Nico Reyes", Icons.Outlined.Person)

                Spacer(Modifier.height(14.dp))
                AuthLabel("Username")
                AuthField(username, { username = it }, "Choose a username", Icons.Outlined.Badge)

                Spacer(Modifier.height(14.dp))
                AuthLabel("Temporary password")
                AuthField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "At least 6 characters",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                Spacer(Modifier.height(14.dp))
                AuthLabel("Confirm password")
                AuthField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    placeholder = "Re-enter password",
                    leadingIcon = Icons.Outlined.Lock,
                    isPassword = true,
                    passwordVisible = passwordVisible,
                    onTogglePassword = { passwordVisible = !passwordVisible }
                )

                // Role (fixed): the Owner can only create Employee accounts
                Spacer(Modifier.height(16.dp))
                AuthLabel("Role")
                val roleShape = RoundedCornerShape(16.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, roleShape, ambientColor = AuthRaspberry, spotColor = AuthRaspberry)
                        .clip(roleShape)
                        .background(Brush.horizontalGradient(listOf(AuthRaspberry, AuthRaspberryDark)))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Outlined.Inventory2, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Employee", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "Daily operations", color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp, lineHeight = 12.sp
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    "Employees can record Stock In and Stock Out and view inventory. " +
                        "They cannot add, edit or delete beverages. Share the temporary password " +
                        "with them in person.",
                    color = AuthMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AuthFieldBg)
                        .border(1.dp, AuthFieldBorder, RoundedCornerShape(14.dp))
                        .padding(12.dp)
                )

                Spacer(Modifier.height(20.dp))
                AuthPrimaryButton(text = "Create employee account", onClick = { submit() })

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