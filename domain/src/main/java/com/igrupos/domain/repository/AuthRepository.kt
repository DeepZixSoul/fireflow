package com.igrupos.domain.repository

import com.igrupos.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    suspend fun isLoggedIn(): Boolean
    suspend fun changePassword(userId: Long, oldPassword: String, newPassword: String): Result<Unit>
    suspend fun clearMustChangePassword(userId: Long)
}
