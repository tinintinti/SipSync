package com.example.sipsync.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// onRegister reports back through onResult: null = success, otherwise an error message.
// replacing = true when a store already exists and will be erased.
@Composable
fun RegisterStoreScreen(
    replacing: Boolean = false,
    onBack: () -> Unit,
    onRegister: (
        storeName: String,
        ownerName: String,
        username: String,
        password: String,
        onResult: (String?) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    BackHandler(onBack = onBack)
    DarkStatusBarIcons()

    var storeName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    fun submit() {
        val error: String? = when {
            storeName.isBlank() -> "Please enter the store name"
            ownerName.isBlank() -> "Please enter the owner's full name"
            username.trim().length < 3 -> "Username must be at least 3 characters"
            password.length < 6 -> "Password must be at least 6 characters"
            password != confirm -> "Passwords do not match"
            else -> null
        }
        if (error != null) {
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            return
        }
        onRegister(storeName.trim(), ownerName.trim(), username.trim(), password) { dbError ->
            if (dbError != null) Toast.makeText(context, dbError, Toast.LENGTH_SHORT).show()
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
            // Header
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
                        "OWNER SETUP", color = AuthRaspberry, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp
                    )
                    Text(
                        if (replacing) "Register a new store" else "Register your store",
                        color = AuthPlum, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            AuthCard {
                AuthLabel("Store name")
                AuthField(storeName, { storeName = it }, "e.g. Santos Convenience", Icons.Outlined.Storefront)

                Spacer(Modifier.height(14.dp))
                AuthLabel("Owner full name")
                AuthField(ownerName, { ownerName = it }, "e.g. Mara Santos", Icons.Outlined.Person)

                Spacer(Modifier.height(14.dp))
                AuthLabel("Username")
                AuthField(username, { username = it }, "Choose a username", Icons.Outlined.Person)

                Spacer(Modifier.height(14.dp))
                AuthLabel("Password")
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

                Spacer(Modifier.height(16.dp))
                Text(
                    if (replacing) {
                        "This replaces the current store on this phone. All of its data will be erased."
                    } else {
                        "Only the Owner registers a store. The Employee account is created later from the Owner's Profile."
                    },
                    color = AuthMuted,
                    fontSize = 11.sp,
                    lineHeight = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(AuthFieldBg)
                        .padding(12.dp)
                )

                Spacer(Modifier.height(20.dp))
                AuthPrimaryButton(text = "Create account", onClick = { submit() })

                Spacer(Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Already registered? ", color = AuthMuted, fontSize = 13.sp)
                    Text(
                        "Back to log in",
                        color = AuthRaspberry,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clickable(role = Role.Button, onClick = onBack)
                            .padding(vertical = 8.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}