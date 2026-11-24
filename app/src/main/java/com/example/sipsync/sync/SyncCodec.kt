package com.example.sipsync.sync

import com.example.sipsync.data.BeverageEntity
import com.example.sipsync.data.TransactionEntity
import com.example.sipsync.data.UserEntity
import org.json.JSONArray
import org.json.JSONObject

/** What the Sync screen shows. */
sealed class SyncStatus {
    object Idle : SyncStatus()
    object Searching : SyncStatus()
    data class Connecting(val peer: String) : SyncStatus()
    data class Syncing(val peer: String) : SyncStatus()
    data class Done(val peer: String, val sent: Int, val received: Int) : SyncStatus()
    data class Failed(val reason: String) : SyncStatus()
}

/** The data one phone sends to the other. */
data class SyncPayload(
    val role: String, // "OWNER" or "EMPLOYEE"
    val users: List<UserEntity>,
    val beverages: List<BeverageEntity>,
    val transactions: List<TransactionEntity>
)

object SyncCodec {

    fun encode(p: SyncPayload): String {
        val root = JSONObject()
        root.put("role", p.role)

        val users = JSONArray()
        p.users.forEach { u ->
            users.put(
                JSONObject()
                    .put("id", u.id)
                    .put("username", u.username)
                    .put("passwordHash", u.passwordHash)
                    .put("salt", u.salt)
                    .put("role", u.role)
                    .put("fullName", u.fullName)
            )
        }
        root.put("users", users)

        val beverages = JSONArray()
        p.beverages.forEach { b ->
            beverages.put(
                JSONObject()
                    .put("id", b.id)
                    .put("name", b.name)
                    .put("category", b.category)
                    .put("price", b.price)
                    .put("minStock", b.minStock)
                    .put("isDeleted", b.isDeleted)
                    .put("updatedAt", b.updatedAt)
            )
        }
        root.put("beverages", beverages)

        val txns = JSONArray()
        p.transactions.forEach { t ->
            txns.put(
                JSONObject()
                    .put("id", t.id)
                    .put("beverageId", t.beverageId)
                    .put("userId", t.userId)
                    .put("quantity", t.quantity)
                    .put("createdAt", t.createdAt)
                    .put("deviceId", t.deviceId)
            )
        }
        root.put("transactions", txns)

        return root.toString()
    }

    fun decode(text: String): SyncPayload {
        val root = JSONObject(text)

        val usersJson = root.getJSONArray("users")
        val users = List(usersJson.length()) { i ->
            val o = usersJson.getJSONObject(i)
            UserEntity(
                id = o.getString("id"),
                username = o.getString("username"),
                passwordHash = o.getString("passwordHash"),
                salt = o.getString("salt"),
                role = o.getString("role"),
                fullName = o.getString("fullName")
            )
        }

        val bevJson = root.getJSONArray("beverages")
        val beverages = List(bevJson.length()) { i ->
            val o = bevJson.getJSONObject(i)
            BeverageEntity(
                id = o.getString("id"),
                name = o.getString("name"),
                category = o.getString("category"),
                price = o.getDouble("price"),
                minStock = o.getInt("minStock"),
                isDeleted = o.getBoolean("isDeleted"),
                updatedAt = o.getLong("updatedAt")
            )
        }

        val txnJson = root.getJSONArray("transactions")
        val transactions = List(txnJson.length()) { i ->
            val o = txnJson.getJSONObject(i)
            TransactionEntity(
                id = o.getString("id"),
                beverageId = o.getString("beverageId"),
                userId = o.getString("userId"),
                quantity = o.getInt("quantity"),
                createdAt = o.getLong("createdAt"),
                deviceId = o.getString("deviceId"),
                synced = true // it came from the other phone, so both phones already have it
            )
        }

        return SyncPayload(root.getString("role"), users, beverages, transactions)
    }
}