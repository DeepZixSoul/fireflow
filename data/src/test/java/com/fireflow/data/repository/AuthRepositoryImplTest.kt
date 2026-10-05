package com.fireflow.data.repository

import com.fireflow.data.mapper.toDomain
import com.fireflow.database.dao.UserDao
import com.fireflow.database.entity.UserEntity
import com.fireflow.domain.model.UserRole
import com.fireflow.security.PasswordHasher
import com.fireflow.security.SessionManager
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryImplTest {

    private lateinit var userDao: UserDao
    private lateinit var passwordHasher: PasswordHasher
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: AuthRepositoryImpl

    private val testEntity = UserEntity(
        id = 1L,
        username = "admin",
        displayName = "Admin",
        email = "admin@test.com",
        passwordHash = "hashed_password",
        role = "ADMIN",
        isActive = true,
        mustChangePassword = false
    )

    @Before
    fun setup() {
        userDao = mockk(relaxed = true)
        passwordHasher = mockk(relaxed = true)
        sessionManager = mockk(relaxed = true)
        repository = AuthRepositoryImpl(userDao, passwordHasher, sessionManager)
    }

    @Test
    fun `login success with valid credentials`() = runTest {
        coEvery { userDao.getByUsername("admin") } returns testEntity
        coEvery { passwordHasher.verify("Admin123", "hashed_password") } returns true
        coEvery { sessionManager.saveSession(any(), any(), any(), any()) } returns Unit

        val result = repository.login("admin", "Admin123")

        assertTrue(result.isSuccess)
        assertEquals("admin", result.getOrNull()?.username)
        coVerify { sessionManager.saveSession(1L, "admin", "Admin", "ADMIN") }
    }

    @Test
    fun `login fails with wrong username`() = runTest {
        coEvery { userDao.getByUsername("wrong") } returns null

        val result = repository.login("wrong", "Admin123")

        assertTrue(result.isFailure)
        assertEquals("Usuario no encontrado", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails with wrong password`() = runTest {
        coEvery { userDao.getByUsername("admin") } returns testEntity
        coEvery { passwordHasher.verify("wrong", "hashed_password") } returns false

        val result = repository.login("admin", "wrong")

        assertTrue(result.isFailure)
        assertEquals("Contraseña incorrecta", result.exceptionOrNull()?.message)
    }

    @Test
    fun `login fails with inactive user`() = runTest {
        coEvery { userDao.getByUsername("admin") } returns testEntity.copy(isActive = false)

        val result = repository.login("admin", "Admin123")

        assertTrue(result.isFailure)
        assertEquals("Usuario desactivado", result.exceptionOrNull()?.message)
    }

    @Test
    fun `logout clears session`() = runTest {
        repository.logout()
        coVerify { sessionManager.clearSession() }
    }

    @Test
    fun `getCurrentUser returns user when logged in`() = runTest {
        coEvery { sessionManager.getUserId() } returns 1L
        coEvery { userDao.getById(1L) } returns testEntity

        val user = repository.getCurrentUser()

        assertNotNull(user)
        assertEquals("admin", user?.username)
    }

    @Test
    fun `getCurrentUser returns null when no session`() = runTest {
        coEvery { sessionManager.getUserId() } returns null

        val user = repository.getCurrentUser()

        assertNull(user)
    }

    @Test
    fun `changePassword success`() = runTest {
        coEvery { userDao.getById(1L) } returns testEntity
        coEvery { passwordHasher.verify("OldPass1", "hashed_password") } returns true
        coEvery { passwordHasher.hash("NewPass1A") } returns "new_hashed"
        coEvery { userDao.updatePassword(any(), any(), any()) } returns Unit

        val result = repository.changePassword(1L, "OldPass1", "NewPass1A")

        assertTrue(result.isSuccess)
        coVerify { userDao.updatePassword(1L, "new_hashed", any()) }
    }

    @Test
    fun `changePassword fails with wrong old password`() = runTest {
        coEvery { userDao.getById(1L) } returns testEntity
        coEvery { passwordHasher.verify("WrongOld", "hashed_password") } returns false

        val result = repository.changePassword(1L, "WrongOld", "NewPass1A")

        assertTrue(result.isFailure)
        assertEquals("Contraseña actual incorrecta", result.exceptionOrNull()?.message)
    }

    @Test
    fun `changePassword fails with non-existent user`() = runTest {
        coEvery { userDao.getById(999L) } returns null

        val result = repository.changePassword(999L, "OldPass1", "NewPass1A")

        assertTrue(result.isFailure)
        assertEquals("Usuario no encontrado", result.exceptionOrNull()?.message)
    }

    @Test
    fun `clearMustChangePassword delegates to dao`() = runTest {
        coEvery { userDao.clearMustChangePassword(any(), any()) } returns Unit

        repository.clearMustChangePassword(1L)

        coVerify { userDao.clearMustChangePassword(1L, any()) }
    }
}
