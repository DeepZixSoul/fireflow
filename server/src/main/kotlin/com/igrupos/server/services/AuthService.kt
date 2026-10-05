package com.igrupos.server.services

import com.igrupos.server.models.request.LoginRequest
import com.igrupos.server.models.response.LoginResponse
import com.igrupos.server.repositories.UserRepository
import com.igrupos.server.utils.JwtUtils
import com.igrupos.server.utils.PasswordUtils
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class AuthService(private val userRepository: UserRepository) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    private data class FailedLoginRecord(
        val timestamps: MutableList<Long> = mutableListOf()
    )

    private val failedLogins = ConcurrentHashMap<String, FailedLoginRecord>()
    private val maxFailedAttempts = 5
    private val lockoutWindowMs = 900_000L // 15 minutes

    fun resetFailedLogins() {
        failedLogins.clear()
    }

    private fun isUserLockedOut(username: String): Boolean {
        val record = failedLogins[username] ?: return false
        val now = System.currentTimeMillis()
        synchronized(record.timestamps) {
            record.timestamps.removeAll { now - it > lockoutWindowMs }
            return record.timestamps.size >= maxFailedAttempts
        }
    }

    private fun recordFailedLogin(username: String) {
        val now = System.currentTimeMillis()
        val record = failedLogins.getOrPut(username) { FailedLoginRecord() }
        synchronized(record.timestamps) {
            record.timestamps.removeAll { now - it > lockoutWindowMs }
            record.timestamps.add(now)
        }
    }

    private fun clearFailedLogins(username: String) {
        failedLogins.remove(username)
    }

    fun login(request: LoginRequest): Result<LoginResponse> {
        return try {
            val unifiedError = Exception("Credenciales inválidas")

            if (isUserLockedOut(request.username)) {
                logger.warn("Login blocked: too many failed attempts")
                return Result.failure(Exception("Demasiados intentos. Intente más tarde."))
            }

            val user = userRepository.findByUsername(request.username)

            if (user == null) {
                logger.warn("Login failed: user not found")
                return Result.failure(unifiedError)
            }

            if (!user[com.igrupos.server.repositories.Users.isActive]) {
                logger.warn("Login failed: user is inactive")
                return Result.failure(unifiedError)
            }

            if (!PasswordUtils.verifyPassword(request.password, user[com.igrupos.server.repositories.Users.passwordHash])) {
                recordFailedLogin(request.username)
                logger.warn("Login failed: invalid password")
                return Result.failure(unifiedError)
            }

            clearFailedLogins(request.username)

            val token = JwtUtils.generateToken(
                userId = user[com.igrupos.server.repositories.Users.id],
                username = user[com.igrupos.server.repositories.Users.username],
                role = user[com.igrupos.server.repositories.Users.role],
                passwordChangedAt = user[com.igrupos.server.repositories.Users.passwordChangedAt]
            )

            logger.info("Login successful for user=${request.username}")

            Result.success(
                LoginResponse(
                    token = token,
                    expiresIn = JwtUtils.getExpirationMs(),
                    role = user[com.igrupos.server.repositories.Users.role],
                    username = user[com.igrupos.server.repositories.Users.username],
                    mustChangePassword = user[com.igrupos.server.repositories.Users.mustChangePassword]
                )
            )
        } catch (e: Exception) {
            logger.error("Login error", e)
            Result.failure(e)
        }
    }

    fun changePassword(userId: Long, newPassword: String): Result<Unit> {
        return try {
            val validation = PasswordUtils.validatePassword(newPassword)
            if (!validation.isValid) {
                val message = "Password inválido: ${validation.errors.joinToString(", ")}"
                return Result.failure(Exception(message))
            }

            val hash = PasswordUtils.hashPassword(newPassword)
            userRepository.updatePassword(userId, hash)
            logger.info("Password changed successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            logger.error("Password change error", e)
            Result.failure(e)
        }
    }
}
