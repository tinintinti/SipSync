package com.example.sipsync.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users", indices = [Index(value = ["username"], unique = true)])
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val passwordHash: String,
    val salt: String,
    val role: String, // "OWNER" or "EMPLOYEE"
    val fullName: String
)

@Entity(tableName = "beverages")
data class BeverageEntity(
    @PrimaryKey val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val minStock: Int,
    val isDeleted: Boolean = false,
    val updatedAt: Long
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val beverageId: String,
    val userId: String,
    val quantity: Int, // positive = Stock In, negative = Stock Out
    val createdAt: Long,
    val deviceId: String,
    val synced: Boolean = false
)

data class BeverageWithStock(
    val id: String,
    val name: String,
    val category: String,
    val price: Double,
    val minStock: Int,
    val isDeleted: Boolean,
    val updatedAt: Long,
    val units: Int
)