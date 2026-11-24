package com.example.sipsync.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): UserEntity?

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity): Long

    @Query("UPDATE users SET fullName = :name WHERE id = :id")
    suspend fun updateName(id: String, name: String)

    @Query("UPDATE users SET passwordHash = :hash, salt = :salt WHERE id = :id")
    suspend fun updatePassword(id: String, hash: String, salt: String)

    // Used by sync
    @Query("SELECT * FROM users")
    suspend fun getAll(): List<UserEntity>

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    suspend fun countByRole(role: String): Int

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    fun observeCountByRole(role: String): Flow<Int>

    @Query("SELECT * FROM users WHERE role = 'EMPLOYEE' ORDER BY fullName")
    fun observeEmployees(): Flow<List<UserEntity>>

    @Query("DELETE FROM users WHERE id = :id AND role = 'EMPLOYEE'")
    suspend fun deleteEmployee(id: String)

    // Used by sync, to apply the Owner's list of employee accounts
    @Query("DELETE FROM users WHERE role = 'EMPLOYEE'")
    suspend fun deleteAllEmployees()

    @Query("DELETE FROM users WHERE role = 'EMPLOYEE' AND id NOT IN (:keepIds)")
    suspend fun deleteEmployeesNotIn(keepIds: List<String>)
}

@Dao
interface BeverageDao {
    @Query(
        """SELECT b.*, COALESCE(SUM(t.quantity), 0) AS units
           FROM beverages b LEFT JOIN transactions t ON t.beverageId = b.id
           WHERE b.isDeleted = 0
           GROUP BY b.id
           ORDER BY b.name"""
    )
    fun observeWithStock(): Flow<List<BeverageWithStock>>

    @Query("SELECT COUNT(*) FROM beverages")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(beverage: BeverageEntity)

    @Query("UPDATE beverages SET isDeleted = 1, updatedAt = :now WHERE id = :id")
    suspend fun softDelete(id: String, now: Long)

    // Used by sync (includes deleted rows so deletions reach the other phone)
    @Query("SELECT * FROM beverages")
    suspend fun getAll(): List<BeverageEntity>

    @Query("SELECT * FROM beverages WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): BeverageEntity?
}

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(txn: TransactionEntity)

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY createdAt DESC")
    fun observeForUser(userId: String): Flow<List<TransactionEntity>>

    @Query("SELECT COUNT(*) FROM transactions WHERE synced = 0")
    fun observePendingCount(): Flow<Int>

    // Used by sync
    @Query("SELECT * FROM transactions WHERE synced = 0 ORDER BY createdAt")
    suspend fun getUnsynced(): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(txns: List<TransactionEntity>)

    @Query("UPDATE transactions SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<String>)
}