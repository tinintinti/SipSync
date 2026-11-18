package com.example.sipsync.data

import android.content.Context
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID

class Repository(private val db: AppDatabase, context: Context) {

    private val prefs = context.getSharedPreferences("sipsync", Context.MODE_PRIVATE)

    val deviceId: String
        get() = prefs.getString("deviceId", null)
            ?: UUID.randomUUID().toString().also {
                prefs.edit().putString("deviceId", it).apply()
            }

    suspend fun hasUsers(): Boolean = db.userDao().count() > 0

    // Returns false if the username is already taken
    suspend fun createUser(username: String, password: String, fullName: String, role: String): Boolean {
        val user = newUser(UUID.randomUUID().toString(), username.trim(), password, role, fullName.trim())
        return db.userDao().insert(user) != -1L
    }

    // Returns the user if username + password are correct, otherwise null
    suspend fun login(username: String, password: String): UserEntity? {
        val user = db.userDao().findByUsername(username) ?: return null
        return if (hash(password, user.salt) == user.passwordHash) user else null
    }

    private fun newUser(id: String, username: String, password: String, role: String, fullName: String): UserEntity {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toHex()
        return UserEntity(id, username, hash(password, salt), salt, role, fullName)
    }

    private fun hash(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256").digest((salt + password).toByteArray()).toHex()

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}