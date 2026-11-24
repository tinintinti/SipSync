package com.example.sipsync

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sipsync.data.AppDatabase
import com.example.sipsync.data.BeverageWithStock
import com.example.sipsync.data.Repository
import com.example.sipsync.data.TransactionEntity
import com.example.sipsync.data.UserEntity
import com.example.sipsync.sync.SyncStatus
import com.example.sipsync.sync.WifiDirectSync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(app: Application) : AndroidViewModel(app) {
    val repo = Repository(AppDatabase.get(app), app)

    // ---------- Wi-Fi Direct sync ----------
    private val sync = WifiDirectSync(app, repo)
    val syncStatus: StateFlow<SyncStatus> = sync.status

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    // null = still checking, true = no store yet, false = a store exists
    private val _needsSetup = MutableStateFlow<Boolean?>(null)
    val needsSetup: StateFlow<Boolean?> = _needsSetup

    // ---------- Live data from the database ----------
    val beverages: StateFlow<List<BeverageWithStock>> =
        repo.beverages.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Owner sees every record, Employee sees only their own
    val transactions: StateFlow<List<TransactionEntity>> =
        _user.flatMapLatest { u ->
            when {
                u == null -> flowOf(emptyList())
                u.role == "OWNER" -> repo.allTransactions
                else -> repo.transactionsFor(u.id)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val pendingCount: StateFlow<Int> =
        repo.pendingCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val employeeCount: StateFlow<Int> =
        repo.employeeCount.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val employees: StateFlow<List<UserEntity>> =
        repo.employees.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch { _needsSetup.value = !repo.hasUsers() }
        // After a sync, sign out if the Owner removed this account
        viewModelScope.launch {
            sync.status.collect { s ->
                if (s is SyncStatus.Done) {
                    if (_user.value == null) {
                        // Synced from the login screen: accounts may have arrived
                        _needsSetup.value = !repo.hasUsers()
                    } else {
                        signOutIfRemoved()
                    }
                }
            }
        }
    }

    // ---------- Login and store ----------
    fun login(username: String, password: String, role: String) {
        viewModelScope.launch {
            _loginError.value = null
            val u = repo.login(username, password)
            when {
                u == null -> _loginError.value = "Wrong username or password"
                !u.role.equals(role, ignoreCase = true) ->
                    _loginError.value = "This account is not an $role account"
                else -> _user.value = u
            }
        }
    }

    fun registerOwner(
        storeName: String,
        ownerName: String,
        username: String,
        password: String,
        replaceExisting: Boolean,
        onResult: (String?) -> Unit
    ) {
        viewModelScope.launch {
            val exists = repo.hasUsers()
            if (exists && !replaceExisting) {
                onResult("A store is already registered on this phone")
                return@launch
            }
            if (exists) {
                withContext(Dispatchers.IO) {
                    AppDatabase.get(getApplication()).clearAllTables()
                }
            }
            if (!repo.createUser(username, password, ownerName, "OWNER")) {
                onResult("That username is already taken")
                return@launch
            }
            getApplication<Application>()
                .getSharedPreferences("store", Context.MODE_PRIVATE)
                .edit()
                .putString("storeName", storeName)
                .apply()

            _loginError.value = null
            _needsSetup.value = false
            _user.value = repo.login(username.trim(), password)
            onResult(null)
        }
    }

    fun createEmployee(
        fullName: String,
        username: String,
        password: String,
        onResult: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            if (_user.value?.role != "OWNER") {
                onResult(false) // only the Owner creates accounts
                return@launch
            }
            onResult(repo.createEmployee(username, password, fullName))
        }
    }

    // Only the Owner can delete the employee account
    fun deleteEmployee(id: String) {
        if (_user.value?.role != "OWNER") return
        viewModelScope.launch { repo.deleteEmployee(id) }
    }

    // If this phone's account was removed by the Owner (found out during sync), sign out
    private suspend fun signOutIfRemoved() {
        val u = _user.value ?: return
        if (repo.userById(u.id) == null) {
            sync.stop()
            _loginError.value = "This account was removed by the Owner"
            _user.value = null
        }
    }

    // Only the Owner can edit the display name
    fun updateName(newName: String) {
        val u = _user.value ?: return
        if (u.role != "OWNER") return
        viewModelScope.launch {
            repo.updateName(u.id, newName)
            _user.value = repo.userById(u.id)
        }
    }

    // onResult(true) = changed, false = current password was wrong
    fun changePassword(current: String, newPassword: String, onResult: (Boolean) -> Unit) {
        val u = _user.value ?: return
        viewModelScope.launch {
            val ok = repo.changePassword(u, current, newPassword)
            if (ok) _user.value = repo.userById(u.id) // refresh the stored hash and salt
            onResult(ok)
        }
    }

    fun logout() {
        sync.stop()
        _user.value = null
    }

    // ---------- Inventory actions ----------
    // signedQuantity: positive = Stock In, negative = Stock Out
    fun recordStock(beverageId: String, signedQuantity: Int) {
        val u = _user.value ?: return
        viewModelScope.launch { repo.recordStock(beverageId, u.id, signedQuantity) }
    }

    fun addBeverage(
        name: String,
        category: String,
        price: Double,
        minStock: Int,
        initialStock: Int
    ) {
        val u = _user.value ?: return
        if (u.role != "OWNER") return // only the Owner changes the catalog
        viewModelScope.launch {
            repo.addBeverage(name, category, price, minStock, initialStock, u.id)
        }
    }

    fun deleteBeverage(id: String) {
        if (_user.value?.role != "OWNER") return
        viewModelScope.launch { repo.deleteBeverage(id) }
    }

    // ---------- Sync actions (called from the Sync screen) ----------
    // Start advertising so the other phone can find this one
    fun startSync() {
        val u = _user.value ?: return
        sync.start(u.role)
    }

    // "Tap to Sync": search for the other phone and exchange data
    fun syncNow() {
        sync.syncNow()
    }

    // Used from the login screen, before anyone is logged in (Employee phone)
    fun startSyncFromLogin() {
        sync.start("EMPLOYEE")
    }

    fun stopSync() {
        sync.stop()
    }

    override fun onCleared() {
        sync.close()
        super.onCleared()
    }
}