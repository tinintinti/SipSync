package com.example.sipsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.sipsync.screens.LoginScreen
import com.example.sipsync.screens.MainScreen
import com.example.sipsync.screens.SetupScreen
import com.example.sipsync.screens.SplashScreen

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

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
                var splashDone by rememberSaveable { mutableStateOf(false) }

                val user by vm.user.collectAsState()
                val loginError by vm.loginError.collectAsState()
                val needsSetup by vm.needsSetup.collectAsState()
                val isOwner = user?.role == "OWNER"

                // 0 = splash, 1 = login, 2 = main app, 3 = first-time owner setup
                val stage = when {
                    !splashDone -> 0
                    user != null -> 2
                    needsSetup == true -> 3
                    else -> 1
                }

                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF073B3A))) {
                    Crossfade(
                        targetState = stage,
                        animationSpec = tween(400),
                        label = "root"
                    ) { current ->
                        when (current) {
                            0 -> SplashScreen(onFinished = { splashDone = true })
                            1 -> LoginScreen(
                                loginError = loginError,
                                onLogin = { u, p -> vm.login(u, p) }
                            )
                            3 -> SetupScreen(
                                error = loginError,
                                onCreate = { name, u, p -> vm.createOwner(name, u, p) }
                            )
                            else -> MainScreen(
                                isOwner = isOwner,
                                userName = user?.fullName ?: "",
                                onAddEmployee = { n, u, p, done -> vm.createEmployee(n, u, p, done) },
                                onLogout = { vm.logout() }      
                            )
                        }
                    }
                }
            }
        }
    }
}