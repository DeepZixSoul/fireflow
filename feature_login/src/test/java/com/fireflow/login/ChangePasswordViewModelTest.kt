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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var authRepository: AuthRepository
    private lateinit var viewModel: ChangePasswordViewModel

    private val testUser = User(
        id = 1L,
        username = "admin",
        displayName = "Admin",
        email = "admin@test.com",
        role = UserRole.ADMIN,
        isActive = true,
        mustChangePassword = true
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        authRepository = mockk(relaxed = true)
        coEvery { authRepository.getCurrentUser() } returns testUser
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun createViewModel(): ChangePasswordViewModel {
        return ChangePasswordViewModel(authRepository)
    }

    @Test
    fun `init loads current user`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(testUser, viewModel.uiState.value.user)
    }

    @Test
    fun `changePassword with blank fields shows error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.changePassword("", "NewPass1", "NewPass1")
        advanceUntilIdle()

        assertEquals(R.string.error_fill_all_fields, viewModel.uiState.value.error)
    }

    @Test
    fun `changePassword with mismatched passwords shows error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.changePassword("OldPass1", "NewPass1", "Different1")
        advanceUntilIdle()

        assertEquals(R.string.error_passwords_dont_match, viewModel.uiState.value.error)
    }

    @Test
    fun `changePassword with short password shows error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.changePassword("OldPass1", "Ab1", "Ab1")
        advanceUntilIdle()

        assertEquals(R.string.error_password_too_short, viewModel.uiState.value.error)
    }

    @Test
    fun `changePassword with invalid complexity shows validation errors`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.changePassword("OldPass1", "alllowercase", "alllowercase")
        advanceUntilIdle()

        assertEquals(R.string.error_password_validation, viewModel.uiState.value.error)
    }

    @Test
    fun `successful password change sets success`() = runTest {
        coEvery { authRepository.changePassword(1L, "OldPass1", "NewPass1A") } returns Result.success(Unit)

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.changePassword("OldPass1", "NewPass1A", "NewPass1A")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.success)
        coVerify { authRepository.clearMustChangePassword(1L) }
    }

    @Test
    fun `failed password change shows error`() = runTest {
        coEvery { authRepository.changePassword(1L, "OldPass1", "NewPass1A") } returns Result.failure(Exception("Contraseña actual incorrecta"))

        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.changePassword("OldPass1", "NewPass1A", "NewPass1A")
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.success)
        assertEquals(R.string.error_change_password, viewModel.uiState.value.error)
    }

    @Test
    fun `clearError removes error`() = runTest {
        viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.changePassword("", "", "")
        advanceUntilIdle()

        viewModel.clearError()
        assertNull(viewModel.uiState.value.error)
    }
}
