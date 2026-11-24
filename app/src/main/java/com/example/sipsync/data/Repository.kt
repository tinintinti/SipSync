package com.example.sipsync.data

import android.content.Context
import androidx.room.withTransaction
import com.example.sipsync.sync.SyncPayload
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.UUID
import kotlinx.coroutines.flow.Flow

// How many Employee accounts the Owner may create
const val MAX_EMPLOYEES = 1

class Repository(private val db: AppDatabase, context: Context) {

    private val prefs = context.getSharedPreferences("sipsync", Context.MODE_PRIVATE)

    val deviceId: String
        get() = prefs.getString("deviceId", null)
            ?: UUID.randomUUID().toString().also {
                prefs.edit().putString("deviceId", it).apply()
            }

    // ---------- Users ----------
    suspend fun hasUsers(): Boolean = db.userDao().count() > 0

    // Returns false if the username is already taken
    suspend fun createUser(username: String, password: String, fullName: String, role: String): Boolean {
        val user = newUser(UUID.randomUUID().toString(), username.trim(), password, role, fullName.trim())
        return db.userDao().insert(user) != -1L
    }

    // Live number of employee accounts (the UI uses it to block the "add employee" form)
    val employeeCount: Flow<Int> = db.userDao().observeCountByRole("EMPLOYEE")

    // Returns false if the username is taken OR the employee limit is already reached
    suspend fun createEmployee(username: String, password: String, fullName: String): Boolean {
        if (db.userDao().countByRole("EMPLOYEE") >= MAX_EMPLOYEES) return false
        return createUser(username, password, fullName, "EMPLOYEE")
    }

    // The employee accounts, for the Owner's "Manage Employee" page
    val employees: Flow<List<UserEntity>> = db.userDao().observeEmployees()

    // Removes an employee account. Their past transactions stay in History.
    suspend fun deleteEmployee(id: String) {
        db.userDao().deleteEmployee(id)
    }

    // Returns the user if username + password are correct, otherwise null
    suspend fun login(username: String, password: String): UserEntity? {
        val user = db.userDao().findByUsername(username) ?: return null
        return if (hash(password, user.salt) == user.passwordHash) user else null
    }

    suspend fun userById(id: String): UserEntity? = db.userDao().findById(id)

    suspend fun updateName(userId: String, name: String) {
        db.userDao().updateName(userId, name.trim())
    }

    // Returns false if the current password is wrong
    suspend fun changePassword(user: UserEntity, current: String, newPassword: String): Boolean {
        if (hash(current, user.salt) != user.passwordHash) return false
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toHex()
        db.userDao().updatePassword(user.id, hash(newPassword, salt), salt)
        return true
    }

    // ---------- Beverages ----------
    // Each beverage with its stock (the sum of its transactions)
    val beverages: Flow<List<BeverageWithStock>> = db.beverageDao().observeWithStock()

    suspend fun addBeverage(
        name: String,
        category: String,
        price: Double,
        minStock: Int,
        initialStock: Int,
        userId: String
    ) {
        val id = UUID.randomUUID().toString()
        val now = System.currentTimeMillis()
        db.beverageDao().upsert(
            BeverageEntity(
                id = id,
                name = name.trim(),
                category = category,
                price = price,
                minStock = minStock,
                isDeleted = false,
                updatedAt = now
            )
        )
        // Opening stock is recorded as a normal Stock In, so stock stays "sum of transactions"
        if (initialStock > 0) recordStock(id, userId, initialStock)
    }

    suspend fun deleteBeverage(id: String) {
        db.beverageDao().softDelete(id, System.currentTimeMillis())
    }

    // ---------- Transactions ----------
    val allTransactions: Flow<List<TransactionEntity>> = db.transactionDao().observeAll()

    fun transactionsFor(userId: String): Flow<List<TransactionEntity>> =
        db.transactionDao().observeForUser(userId)

    // Records transactions waiting to be sent to the other phone
    val pendingCount: Flow<Int> = db.transactionDao().observePendingCount()

    // signedQuantity: positive = Stock In, negative = Stock Out
    suspend fun recordStock(beverageId: String, userId: String, signedQuantity: Int) {
        db.transactionDao().insert(
            TransactionEntity(
                id = UUID.randomUUID().toString(),
                beverageId = beverageId,
                userId = userId,
                quantity = signedQuantity,
                createdAt = System.currentTimeMillis(),
                deviceId = deviceId,
                synced = false
            )
        )
    }

    // ---------- Wi-Fi Direct sync ----------
    // What this phone sends to the other one:
    //  - Owner:    employee accounts + the whole beverage catalog + unsynced transactions
    //  - Employee: unsynced transactions only (employees never change the catalog)
    // The Owner's own account is never sent.
    suspend fun buildSyncPayload(role: String): SyncPayload {
        val unsynced = db.transactionDao().getUnsynced()
        return if (role == "OWNER") {
            SyncPayload(
                role = role,
                users = db.userDao().getAll().filter { it.role == "EMPLOYEE" },
                beverages = db.beverageDao().getAll(),
                transactions = unsynced
            )
        } else {
            SyncPayload(role, emptyList(), emptyList(), unsynced)
        }
    }

    // Saves what the other phone sent. Received transactions arrive already marked as synced.
    suspend fun applySyncPayload(p: SyncPayload) {
        db.withTransaction {
            if (p.role == "OWNER") {
                // The Owner's list is the truth: remove employee accounts the Owner deleted
                val employeeIds = p.users.filter { it.role == "EMPLOYEE" }.map { it.id }
                if (employeeIds.isEmpty()) {
                    db.userDao().deleteAllEmployees()
                } else {
                    db.userDao().deleteEmployeesNotIn(employeeIds)
                }
                p.users.filter { it.role == "EMPLOYEE" }.forEach { db.userDao().insert(it) }
                // Newest change wins
                p.beverages.forEach { b ->
                    val current = db.beverageDao().findById(b.id)
                    if (current == null || b.updatedAt > current.updatedAt) db.beverageDao().upsert(b)
                }
            }
            db.transactionDao().insertAll(p.transactions)
        }
    }

    suspend fun markTransactionsSynced(ids: List<String>) {
        ids.chunked(500).forEach { db.transactionDao().markSynced(it) }
    }

    // ---------- Password hashing ----------
    private fun newUser(id: String, username: String, password: String, role: String, fullName: String): UserEntity {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }.toHex()
        return UserEntity(id, username, hash(password, salt), salt, role, fullName)
    }

    private fun hash(password: String, salt: String): String =
        MessageDigest.getInstance("SHA-256").digest((salt + password).toByteArray()).toHex()

    private fun ByteArray.toHex() = joinToString("") { "%02x".format(it) }
}