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
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SetupViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: SetupViewModel

    private val validPassword = "FireFlow-Test-Admin-1!"

    private val createdUser = User(
        id = 1L,
        username = "admin",
        displayName = "admin",
        email = "",
        role = UserRole.ADMIN,
        isActive = true,
        mustChangePassword = false
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): SetupViewModel = SetupViewModel(authRepository)

    @Test
    fun `createAdmin with blank fields shows error`() = runTest {
        viewModel = createViewModel()

        viewModel.createAdmin("", "", "")
        advanceUntilIdle()

        assertEquals(R.string.error_fill_all_fields, viewModel.uiState.value.error)
        coVerify(exactly = 0) { authRepository.createAdmin(any(), any()) }
    }

    @Test
    fun `createAdmin with short username shows error`() = runTest {
        viewModel = createViewModel()

        viewModel.createAdmin("ab", validPassword, validPassword)
        advanceUntilIdle()

        assertEquals(R.string.error_setup_username, viewModel.uiState.value.error)
        coVerify(exactly = 0) { authRepository.createAdmin(any(), any()) }
    }

    @Test
    fun `createAdmin with mismatched passwords shows error`() = runTest {
        viewModel = createViewModel()

        viewModel.createAdmin("admin", validPassword, "Different1")
        advanceUntilIdle()

        assertEquals(R.string.error_passwords_dont_match, viewModel.uiState.value.error)
        coVerify(exactly = 0) { authRepository.createAdmin(any(), any()) }
    }

    @Test
    fun `createAdmin with invalid password shows validation error`() = runTest {
        viewModel = createViewModel()

        viewModel.createAdmin("admin", "alllowercase", "alllowercase")
        advanceUntilIdle()

        assertEquals(R.string.error_password_validation, viewModel.uiState.value.error)
        coVerify(exactly = 0) { authRepository.createAdmin(any(), any()) }
    }

    @Test
    fun `successful createAdmin sets complete`() = runTest {
        coEvery { authRepository.createAdmin("admin", validPassword) } returns Result.success(createdUser)
        viewModel = createViewModel()

        viewModel.createAdmin("admin", validPassword, validPassword)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isComplete)
        coVerify(exactly = 1) { authRepository.createAdmin("admin", validPassword) }
    }

    @Test
    fun `failed createAdmin shows error`() = runTest {
        coEvery { authRepository.createAdmin("admin", validPassword) } returns
            Result.failure(Exception("Ya existe una cuenta en esta instalación"))
        viewModel = createViewModel()

        viewModel.createAdmin("admin", validPassword, validPassword)
        advanceUntilIdle()

        assertEquals(R.string.error_setup_failed, viewModel.uiState.value.error)
        assertTrue(!viewModel.uiState.value.isComplete)
    }

    @Test
    fun `clearError removes error`() = runTest {
        viewModel = createViewModel()

        viewModel.createAdmin("", "", "")
        advanceUntilIdle()

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}
