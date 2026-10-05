package com.igrupos.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.igrupos.domain.model.User
import com.igrupos.domain.model.UserRole
import com.igrupos.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var authRepository: AuthRepository

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

    @Test
    fun displaysOldPasswordField() {
        val viewModel = ChangePasswordViewModel(authRepository)
        composeTestRule.setContent {
            ChangePasswordScreen(onPasswordChanged = {}, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Contraseña actual").assertIsDisplayed()
    }

    @Test
    fun displaysNewPasswordField() {
        val viewModel = ChangePasswordViewModel(authRepository)
        composeTestRule.setContent {
            ChangePasswordScreen(onPasswordChanged = {}, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Nueva contraseña").assertIsDisplayed()
    }

    @Test
    fun displaysConfirmPasswordField() {
        val viewModel = ChangePasswordViewModel(authRepository)
        composeTestRule.setContent {
            ChangePasswordScreen(onPasswordChanged = {}, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Confirmar contraseña").assertIsDisplayed()
    }

    @Test
    fun displaysChangePasswordButton() {
        val viewModel = ChangePasswordViewModel(authRepository)
        composeTestRule.setContent {
            ChangePasswordScreen(onPasswordChanged = {}, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Cambiar contraseña").assertIsDisplayed()
    }

    @Test
    fun canTypeInAllFields() {
        val viewModel = ChangePasswordViewModel(authRepository)
        composeTestRule.setContent {
            ChangePasswordScreen(onPasswordChanged = {}, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Contraseña actual").performTextInput("oldpass")
        composeTestRule.onNodeWithText("Nueva contraseña").performTextInput("NewPass1")
        composeTestRule.onNodeWithText("Confirmar contraseña").performTextInput("NewPass1")
    }

    @Test
    fun showsErrorOnMismatchedPasswords() {
        val viewModel = ChangePasswordViewModel(authRepository)
        composeTestRule.setContent {
            ChangePasswordScreen(onPasswordChanged = {}, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Contraseña actual").performTextInput("OldPass1")
        composeTestRule.onNodeWithText("Nueva contraseña").performTextInput("NewPass1A")
        composeTestRule.onNodeWithText("Confirmar contraseña").performTextInput("Different1")
        composeTestRule.onNodeWithText("Cambiar contraseña").performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Las contraseñas no coinciden").assertIsDisplayed()
    }

    @Test
    fun showsErrorOnEmptyFields() {
        val viewModel = ChangePasswordViewModel(authRepository)
        composeTestRule.setContent {
            ChangePasswordScreen(onPasswordChanged = {}, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Cambiar contraseña").performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Completa todos los campos").assertIsDisplayed()
    }
}
