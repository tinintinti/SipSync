package com.example.sipsync

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.sipsync.data.AppDatabase
import com.example.sipsync.data.Repository
import com.example.sipsync.data.UserEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppViewModel(app: Application) : AndroidViewModel(app) {
    val repo = Repository(AppDatabase.get(app), app)

    private val _user = MutableStateFlow<UserEntity?>(null)
    val user: StateFlow<UserEntity?> = _user

    private val _loginError = MutableStateFlow<String?>(null)
    val loginError: StateFlow<String?> = _loginError

    // null = still checking, true = no accounts yet (show setup)
    private val _needsSetup = MutableStateFlow<Boolean?>(null)
    val needsSetup: StateFlow<Boolean?> = _needsSetup

    init {
        viewModelScope.launch { _needsSetup.value = !repo.hasUsers() }
    }

    fun login(username: String, password: String) {
        viewModelScope.launch {
            val u = repo.login(username, password)
            if (u == null) {
                _loginError.value = "Wrong username or password"
            } else {
                _loginError.value = null
                _user.value = u
            }
        }
    }

    fun createOwner(fullName: String, username: String, password: String) {
        viewModelScope.launch {
            if (!repo.createUser(username, password, fullName, "OWNER")) {
                _loginError.value = "That username is already taken"
                return@launch
            }
            _loginError.value = null
            _needsSetup.value = false
            _user.value = repo.login(username.trim(), password)
        }
    }

    fun createEmployee(fullName: String, username: String, password: String, onResult: (Boolean) -> Unit) {
    viewModelScope.launch {
        onResult(repo.createUser(username, password, fullName, "EMPLOYEE"))
    }
}

    fun logout() {
        _user.value = null
    }
}