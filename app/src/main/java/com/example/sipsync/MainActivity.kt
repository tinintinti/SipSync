package com.example.sipsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.sipsync.screens.LoginScreen
import com.example.sipsync.screens.MainScreen
import com.example.sipsync.screens.SplashScreen

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
                // rememberSaveable: the splash does not replay when the screen rotates
                var splashDone by rememberSaveable { mutableStateOf(false) }
                var loggedIn by remember { mutableStateOf(false) }
                var isOwner by remember { mutableStateOf(true) }

                val stage = when {
                    !splashDone -> 0
                    !loggedIn -> 1
                    else -> 2
                }

                // Dark base so the fade from splash to login never flashes white
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF073B3A))) {
                    Crossfade(
                        targetState = stage,
                        animationSpec = tween(400),
                        label = "root"
                    ) { current ->
                        when (current) {
                            0 -> SplashScreen(onFinished = { splashDone = true })
                            1 -> LoginScreen(
                                onLoginSuccess = { role ->
                                    isOwner = role == "Owner"
                                    loggedIn = true
                                }
                            )
                            else -> MainScreen(
                                isOwner = isOwner,
                                onSwitchRole = { isOwner = !isOwner },
                                onLogout = { loggedIn = false }
                            )
                        }
                    }
                }
            }
        }
    }
}