package com.fireflow.login

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.common.R
import com.fireflow.core.util.PasswordValidator
import com.fireflow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SetupUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val isComplete: Boolean = false
)

/**
 * First-run setup: creates the initial administrator account.
 * There are no default credentials in the app, so this screen is mandatory
 * when the installation has no users yet.
 */
@HiltViewModel
class SetupViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    fun createAdmin(username: String, password: String, confirmPassword: String) {
        when {
            username.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                _uiState.value = _uiState.value.copy(error = R.string.error_fill_all_fields)
                return
            }

            username.trim().length < MIN_USERNAME_LENGTH -> {
                _uiState.value = _uiState.value.copy(error = R.string.error_setup_username)
                return
            }

            password != confirmPassword -> {
                _uiState.value = _uiState.value.copy(error = R.string.error_passwords_dont_match)
                return
            }

            !PasswordValidator.validate(password).isValid -> {
                _uiState.value = _uiState.value.copy(error = R.string.error_password_validation)
                return
            }
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            authRepository.createAdmin(username.trim(), password).fold(
                onSuccess = { _uiState.value = SetupUiState(isComplete = true) },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = R.string.error_setup_failed
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private companion object {
        private const val MIN_USERNAME_LENGTH = 3
    }
}
