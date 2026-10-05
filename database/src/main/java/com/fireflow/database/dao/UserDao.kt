package com.fireflow.database.dao

import androidx.room.Dao
import androidx.room.Query
import com.fireflow.database.entity.UserEntity

@Dao
interface UserDao : BaseDao<UserEntity> {
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getByUsername(username: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Long): UserEntity?

    @Query("UPDATE users SET password_hash = :hash, updated_at = :timestamp WHERE id = :id")
    suspend fun updatePassword(id: Long, hash: String, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE users SET must_change_password = 0, updated_at = :timestamp WHERE id = :id")
    suspend fun clearMustChangePassword(id: Long, timestamp: Long = System.currentTimeMillis())
}
