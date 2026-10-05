package com.fireflow.data.repository

import com.fireflow.data.mapper.toDomain
import com.fireflow.database.dao.UserDao
import com.fireflow.domain.model.User
import com.fireflow.domain.repository.AuthRepository
import com.fireflow.security.PasswordHasher
import com.fireflow.security.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val userDao: UserDao,
    private val passwordHasher: PasswordHasher,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val entity = userDao.getByUsername(username)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            if (!entity.isActive) {
                return Result.failure(Exception("Usuario desactivado"))
            }

            if (!passwordHasher.verify(password, entity.passwordHash)) {
                return Result.failure(Exception("Contraseña incorrecta"))
            }

            val user = entity.toDomain()

            sessionManager.saveSession(
                userId = user.id,
                username = user.username,
                displayName = user.displayName,
                role = user.role.name
            )

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override suspend fun getCurrentUser(): User? {
        val userId = sessionManager.getUserId() ?: return null
        return userDao.getById(userId)?.toDomain()
    }

    override suspend fun isLoggedIn(): Boolean {
        return sessionManager.isLoggedIn()
    }

    override suspend fun changePassword(
        userId: Long,
        oldPassword: String,
        newPassword: String
    ): Result<Unit> {
        return try {
            val entity = userDao.getById(userId)
                ?: return Result.failure(Exception("Usuario no encontrado"))

            if (!passwordHasher.verify(oldPassword, entity.passwordHash)) {
                return Result.failure(Exception("Contraseña actual incorrecta"))
            }

            val newHash = passwordHasher.hash(newPassword)
            userDao.updatePassword(userId, newHash)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearMustChangePassword(userId: Long) {
        userDao.clearMustChangePassword(userId)
    }
}
