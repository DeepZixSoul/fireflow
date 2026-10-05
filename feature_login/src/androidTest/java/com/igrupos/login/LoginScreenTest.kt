package com.igrupos.login

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
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
class LoginScreenTest {

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
        mustChangePassword = false
    )

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
    fun displaysUsernameField() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onChangePassword = {}, viewModel = createViewModel())
        }
        composeTestRule.onNodeWithText("Usuario").assertIsDisplayed()
    }

    @Test
    fun displaysPasswordField() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onChangePassword = {}, viewModel = createViewModel())
        }
        composeTestRule.onNodeWithText("Contraseña").assertIsDisplayed()
    }

    @Test
    fun displaysLoginButton() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onChangePassword = {}, viewModel = createViewModel())
        }
        composeTestRule.onNodeWithText("Iniciar sesión").assertIsDisplayed()
    }

    @Test
    fun displaysAppName() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onChangePassword = {}, viewModel = createViewModel())
        }
        composeTestRule.onNodeWithText("IGrupos").assertIsDisplayed()
    }

    @Test
    fun displaysSubtitle() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onChangePassword = {}, viewModel = createViewModel())
        }
        composeTestRule.onNodeWithText("Gestión de grupos de presión").assertIsDisplayed()
    }

    @Test
    fun canTypeUsername() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onChangePassword = {}, viewModel = createViewModel())
        }
        composeTestRule.onNodeWithText("Usuario").performTextInput("admin")
        composeTestRule.onNodeWithText("admin").assertIsDisplayed()
    }

    @Test
    fun canTypePassword() {
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onChangePassword = {}, viewModel = createViewModel())
        }
        composeTestRule.onNodeWithText("Contraseña").performTextInput("secret123")
    }

    @Test
    fun loginButtonDisabledDuringLoading() {
        val viewModel = createViewModel()
        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onChangePassword = {}, viewModel = viewModel)
        }
        composeTestRule.onNodeWithText("Usuario").performTextInput("admin")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("Admin123")
        composeTestRule.onNodeWithText("Iniciar sesión").performClick()
    }

    @Test
    fun showsErrorOnFailedLogin() {
        coEvery { authRepository.login("admin", "wrong") } returns Result.failure(Exception("Credenciales incorrectas"))
        val viewModel = createViewModel()

        composeTestRule.setContent {
            LoginScreen(onLoginSuccess = {}, onChangePassword = {}, viewModel = viewModel)
        }

        composeTestRule.onNodeWithText("Usuario").performTextInput("admin")
        composeTestRule.onNodeWithText("Contraseña").performTextInput("wrong")
        composeTestRule.onNodeWithText("Iniciar sesión").performClick()

        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Credenciales incorrectas").assertIsDisplayed()
    }
}
