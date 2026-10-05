package com.fireflow.login

import com.fireflow.common.R
import com.fireflow.domain.model.User
import com.fireflow.domain.model.UserRole
import com.fireflow.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: LoginViewModel

    private val testUser = User(
        id = 1L,
        username = "admin",
        displayName = "Admin",
        email = "admin@test.com",
        role = UserRole.ADMIN,
        isActive = true,
        mustChangePassword = false
    )

    private val testUserMustChange = testUser.copy(mustChangePassword = true)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        coEvery { authRepository.isLoggedIn() } returns false
        coEvery { authRepository.getCurrentUser() } returns null
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): LoginViewModel {
        return LoginViewModel(authRepository)
    }

    @Test
    fun `initial state is not logged in`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()
        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertFalse(viewModel.uiState.value.isLoading)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `login with blank username shows error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("", "password")
        advanceUntilIdle()

        assertEquals(R.string.error_fill_all_fields, viewModel.uiState.value.error)
    }

    @Test
    fun `login with blank password shows error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("admin", "")
        advanceUntilIdle()

        assertEquals(R.string.error_fill_all_fields, viewModel.uiState.value.error)
    }

    @Test
    fun `successful login sets isLoggedIn true`() = runTest {
        coEvery { authRepository.login("admin", "Admin123") } returns Result.success(testUser)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("admin", "Admin123")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
        assertEquals("admin", viewModel.uiState.value.user?.username)
    }

    @Test
    fun `failed login shows error`() = runTest {
        coEvery { authRepository.login("admin", "wrong") } returns Result.failure(Exception("Credenciales incorrectas"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("admin", "wrong")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoggedIn)
        assertEquals(R.string.error_auth, viewModel.uiState.value.error)
    }

    @Test
    fun `login with mustChangePassword sets mustChangePassword true`() = runTest {
        coEvery { authRepository.login("admin", "Admin123") } returns Result.success(testUserMustChange)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("admin", "Admin123")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.mustChangePassword)
        assertFalse(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `lockout after 5 failed attempts`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("Error"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isLocked)
    }

    @Test
    fun `login is ignored when locked`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("Error"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isLocked)

        viewModel.login("admin", "Admin123")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        coVerify(exactly = 5) { authRepository.login(any(), any()) }
    }

    @Test
    fun `lockout clears after duration`() = runTest {
        coEvery { authRepository.login(any(), any()) } returns Result.failure(Exception("Error"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        viewModel.login("admin", "wrong")
        testScheduler.advanceTimeBy(100)
        testScheduler.runCurrent()

        assertTrue(viewModel.uiState.value.isLocked)

        testScheduler.advanceTimeBy(31_000)
        testScheduler.runCurrent()

        assertFalse(viewModel.uiState.value.isLocked)
        assertEquals(0, viewModel.uiState.value.lockoutSeconds)
    }

    @Test
    fun `clearError removes error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.login("", "")
        advanceUntilIdle()

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `existing session auto-logs in`() = runTest {
        coEvery { authRepository.isLoggedIn() } returns true
        coEvery { authRepository.getCurrentUser() } returns testUser

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isLoggedIn)
    }

    @Test
    fun `existing session with mustChangePassword`() = runTest {
        coEvery { authRepository.isLoggedIn() } returns true
        coEvery { authRepository.getCurrentUser() } returns testUserMustChange

        viewModel = createViewModel()
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.mustChangePassword)
    }
}
