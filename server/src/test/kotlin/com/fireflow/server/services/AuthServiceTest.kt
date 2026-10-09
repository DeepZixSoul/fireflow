package com.fireflow.server.services

import com.fireflow.server.config.DatabaseManager
import com.fireflow.server.config.DatabaseConnectionConfig
import com.fireflow.server.config.JwtConfig
import com.fireflow.server.models.request.LoginRequest
import com.fireflow.server.TestSeed
import com.fireflow.server.repositories.UserRepository
import com.fireflow.server.utils.JwtUtils
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthServiceTest {

    private lateinit var authService: AuthService
    private var adminId: Long = 0

    @BeforeAll
    fun setup() {
        DatabaseManager.init(DatabaseConnectionConfig(
            url = "jdbc:h2:mem:test_auth_service;DB_CLOSE_DELAY=-1",
            driver = "org.h2.Driver",
            user = "sa",
            password = ""
        ))
        JwtUtils.init(JwtConfig(secret = "test-secret-key-for-testing-only-1234567890"))
        val userRepository = UserRepository(DatabaseManager.getDatabase())
        authService = AuthService(userRepository)
        adminId = TestSeed.seedAdmin(userRepository)
    }

    @AfterAll
    fun teardown() {
        DatabaseManager.close()
    }

    @BeforeEach
    fun resetState() {
        authService.resetFailedLogins()
    }

    @Test
    fun `login with valid admin credentials succeeds`() {
        val result = authService.login(LoginRequest(TestSeed.ADMIN_USERNAME, TestSeed.ADMIN_PASSWORD))
        assertTrue(result.isSuccess)

        val response = result.getOrNull()!!
        assertEquals("admin", response.username)
        assertEquals("admin", response.role)
        assertTrue(response.token.isNotBlank())
        assertTrue(response.expiresIn > 0)
    }

    @Test
    fun `login with invalid password fails with unified error`() {
        val result = authService.login(LoginRequest("admin", "wrongpassword"))
        assertTrue(result.isFailure)
        assertEquals("Credenciales inválidas", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login with nonexistent user fails with unified error`() {
        val result = authService.login(LoginRequest("nonexistent", "password"))
        assertTrue(result.isFailure)
        assertEquals("Credenciales inválidas", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login with empty username fails`() {
        val result = authService.login(LoginRequest("", "password"))
        assertTrue(result.isFailure)
    }

    @Test
    fun `login blocks after 5 failed attempts`() {
        for (i in 1..5) {
            authService.login(LoginRequest("admin", "wrongpassword$i"))
        }

        val result = authService.login(LoginRequest("admin", "wrongpassword6"))
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Demasiados intentos") == true)
    }

    @Test
    fun `change password succeeds with valid password`() {
        val result = authService.changePassword(adminId, TestSeed.ADMIN_PASSWORD, "NewPass123")
        assertTrue(result.isSuccess)

        val loginResult = authService.login(LoginRequest("admin", "NewPass123"))
        assertTrue(loginResult.isSuccess)

        // Restore original password for other tests
        val restoreResult = authService.changePassword(adminId, "NewPass123", TestSeed.ADMIN_PASSWORD)
        assertTrue(restoreResult.isSuccess)
    }

    @Test
    fun `change password fails with too short password`() {
        val result = authService.changePassword(adminId, TestSeed.ADMIN_PASSWORD, "ab1")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Password inválido") == true)
    }

    @Test
    fun `change password fails without digit`() {
        val result = authService.changePassword(adminId, TestSeed.ADMIN_PASSWORD, "nodigit")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("número") == true)
    }

    @Test
    fun `change password fails with wrong current password`() {
        val result = authService.changePassword(adminId, "WrongOld1", "NewPass123")
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("contraseña actual") == true)
    }

    @Test
    fun `change password fails when new equals current`() {
        val result = authService.changePassword(adminId, TestSeed.ADMIN_PASSWORD, TestSeed.ADMIN_PASSWORD)
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("distinta") == true)
    }

    @Test
    fun `change password fails for unknown user`() {
        val result = authService.changePassword(999_999L, TestSeed.ADMIN_PASSWORD, "NewPass123")
        assertTrue(result.isFailure)
        assertEquals("Usuario no encontrado", result.exceptionOrNull()?.message)
    }
}
