package com.example.sipsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.sipsync.screens.LoginScreen
import com.example.sipsync.screens.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = false

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF087F70),
                    background = Color(0xFFF8F7F2)
                )
            ) {
                var loggedIn by remember { mutableStateOf(false) }
                var isOwner by remember { mutableStateOf(true) }

                if (loggedIn) {
                    MainScreen(
                        isOwner = isOwner,
                        onSwitchRole = { isOwner = !isOwner },
                        onLogout = { loggedIn = false }
                    )
                } else {
                    LoginScreen(
                        onLoginSuccess = { role ->
                            isOwner = role == "Owner"
                            loggedIn = true
                        }
                    )
                }
            }
        }
    }
}