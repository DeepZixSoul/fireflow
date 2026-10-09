package com.fireflow.domain.repository

import com.fireflow.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun logout()
    suspend fun getCurrentUser(): User?
    suspend fun isLoggedIn(): Boolean
    suspend fun changePassword(userId: Long, oldPassword: String, newPassword: String): Result<Unit>
    suspend fun clearMustChangePassword(userId: Long)

    /** Returns true when at least one user exists (first run shows the setup screen). */
    suspend fun hasUsers(): Boolean

    /**
     * Creates the first administrator account.
     * Fails when a user already exists: no further accounts can be created from the app.
     */
    suspend fun createAdmin(username: String, password: String): Result<User>
}
