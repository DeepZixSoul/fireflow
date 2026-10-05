package com.fireflow.login

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.common.R
import com.fireflow.domain.model.User
import com.fireflow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class LoginUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val isLoggedIn: Boolean = false,
    val mustChangePassword: Boolean = false,
    val isLocked: Boolean = false,
    val lockoutSeconds: Int = 0,
    val user: User? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var failedAttempts = 0

    init {
        checkSession()
    }

    private fun checkSession() {
        viewModelScope.launch {
            val loggedIn = authRepository.isLoggedIn()
            if (loggedIn) {
                val user = authRepository.getCurrentUser()
                if (user != null && user.isActive) {
                    if (user.mustChangePassword) {
                        _uiState.value = LoginUiState(mustChangePassword = true, user = user)
                    } else {
                        _uiState.value = LoginUiState(isLoggedIn = true, user = user)
                    }
                }
            }
        }
    }

    fun login(username: String, password: String) {
        if (_uiState.value.isLocked) return

        if (username.isBlank() || password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = R.string.error_fill_all_fields)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = authRepository.login(username, password)

            result.fold(
                onSuccess = { user ->
                    failedAttempts = 0
                    if (user.mustChangePassword) {
                        _uiState.value = LoginUiState(mustChangePassword = true, user = user)
                    } else {
                        _uiState.value = LoginUiState(isLoggedIn = true, user = user)
                    }
                },
                onFailure = { error ->
                    failedAttempts++
                    if (failedAttempts >= MAX_ATTEMPTS) {
                        startLockout()
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = R.string.error_auth
                        )
                    }
                }
            )
        }
    }

    private fun startLockout() {
        _uiState.value = _uiState.value.copy(
            isLoading = false,
            isLocked = true,
            lockoutSeconds = LOCKOUT_DURATION_SECONDS,
            error = R.string.error_too_many_attempts
        )

        viewModelScope.launch {
            for (seconds in LOCKOUT_DURATION_SECONDS downTo 1) {
                _uiState.value = _uiState.value.copy(lockoutSeconds = seconds)
                delay(1000)
            }
            failedAttempts = 0
            _uiState.value = _uiState.value.copy(
                isLocked = false,
                lockoutSeconds = 0,
                error = null
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    companion object {
        private const val MAX_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_SECONDS = 30
    }
}
