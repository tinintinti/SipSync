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

    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: UserEntity): Long
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
}