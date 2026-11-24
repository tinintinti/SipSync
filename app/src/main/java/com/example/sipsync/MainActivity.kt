package com.example.sipsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import com.example.sipsync.screens.GetAccountSyncDialog
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import com.example.sipsync.screens.EmployeeInfo
import com.example.sipsync.screens.LoginScreen
import com.example.sipsync.screens.MainScreen
import com.example.sipsync.screens.RegisterStoreScreen
import com.example.sipsync.screens.SplashScreen
import com.example.sipsync.screens.buildTxns
import com.example.sipsync.screens.toUi

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        // The new splash and login are light (peach), so the status bar icons must be dark
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        setContent {
            MaterialTheme(
                colorScheme = lightColorScheme(
                    primary = Color(0xFF087F70),
                    background = Color(0xFFF8F7F2)
                )
            ) {
                var splashDone by rememberSaveable { mutableStateOf(false) }
                var showRegister by rememberSaveable { mutableStateOf(false) }
                var showGetAccount by rememberSaveable { mutableStateOf(false) }
                val user by vm.user.collectAsState()
                val loginError by vm.loginError.collectAsState()
                val needsSetup by vm.needsSetup.collectAsState() // null = checking, true = no store yet
                val storeExists = needsSetup == false
                val isOwner = user?.role == "OWNER"

                // Live data from the database, turned into what the screens show
                val dbBeverages by vm.beverages.collectAsState()
                val dbTxns by vm.transactions.collectAsState()
                val pending by vm.pendingCount.collectAsState()
                val dbEmployees by vm.employees.collectAsState()
                val uiEmployees = remember(dbEmployees) {
                    dbEmployees.map { EmployeeInfo(it.id, it.fullName, it.username) }
                }
                val syncStatus by vm.syncStatus.collectAsState()
                val uiBeverages = remember(dbBeverages) { dbBeverages.mapIndexed { i, b -> b.toUi(i) } }
                val uiTxns = remember(dbTxns, uiBeverages, user) { buildTxns(dbTxns, uiBeverages, user) }

                // 0 = splash, 1 = login, 2 = register store, 3 = main app
                val stage = when {
                    !splashDone -> 0
                    user != null -> 3
                    showRegister -> 2
                    else -> 1
                }

                // Peach, so the crossfade from the new splash doesn't flash a dark color
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFEF4F0))) {
                    Crossfade(
                        targetState = stage,
                        animationSpec = tween(400),
                        label = "root"
                    ) { current ->
                        when (current) {
                            0 -> SplashScreen(onFinished = { splashDone = true })

                            1 -> {
                                LoginScreen(
                                    loginError = loginError,
                                    storeExists = storeExists,
                                    onRegisterStore = { showRegister = true },
                                    onGetAccount = { showGetAccount = true },
                                    onLogin = { username, password, role ->
                                        vm.login(username, password, role)
                                    }
                                )
                                if (showGetAccount) {
                                    GetAccountSyncDialog(
                                        status = syncStatus,
                                        onStart = { vm.startSyncFromLogin() },
                                        onSyncNow = { vm.syncNow() },
                                        onStop = { vm.stopSync() },
                                        onClose = { showGetAccount = false }
                                    )
                                }
                            }

                            2 -> RegisterStoreScreen(
                                replacing = storeExists,
                                onBack = { showRegister = false },
                                onRegister = { storeName, ownerName, username, password, onResult ->
                                    vm.registerOwner(
                                        storeName, ownerName, username, password,
                                        replaceExisting = storeExists
                                    ) { error ->
                                        if (error == null) showRegister = false
                                        onResult(error)
                                    }
                                }
                            )

                            else -> MainScreen(
                                isOwner = isOwner,
                                userName = user?.fullName ?: "",
                                beverages = uiBeverages,
                                transactions = uiTxns,
                                pendingCount = pending,
                                onRecordStock = { key, qty -> vm.recordStock(key, qty) },
                                onAddBeverage = { n, c, p, m, s -> vm.addBeverage(n, c, p, m, s) },
                                onDeleteBeverage = { vm.deleteBeverage(it) },
                                onAddEmployee = { n, u, p, done -> vm.createEmployee(n, u, p, done) },
                                deviceId = vm.repo.deviceId,
                                onUpdateName = { vm.updateName(it) },
                                onChangePassword = { current, newPassword, done -> vm.changePassword(current, newPassword, done) },
                                onLogout = { vm.logout() },
                                employees = uiEmployees,
                                onDeleteEmployee = { vm.deleteEmployee(it) },
                                syncStatus = syncStatus,
                                onSyncStart = { vm.startSync() },
                                onSyncNow = { vm.syncNow() },
                                onSyncStop = { vm.stopSync() }
                            )
                        }
                    }
                }
            }
        }
    }
}