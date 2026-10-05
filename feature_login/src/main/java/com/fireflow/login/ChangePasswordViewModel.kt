package com.fireflow.login

import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.common.R
import com.fireflow.core.util.PasswordValidator
import com.fireflow.domain.model.User
import com.fireflow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    @StringRes val error: Int? = null,
    val success: Boolean = false,
    val user: User? = null
)

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser()
            _uiState.value = _uiState.value.copy(user = user)
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, confirmPassword: String) {
        val user = _uiState.value.user
        if (user == null) {
            _uiState.value = _uiState.value.copy(error = R.string.error_invalid_session)
            return
        }

        if (oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank()) {
            _uiState.value = _uiState.value.copy(error = R.string.error_fill_all_fields)
            return
        }

        if (newPassword != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = R.string.error_passwords_dont_match)
            return
        }

        if (newPassword.length < 8) {
            _uiState.value = _uiState.value.copy(error = R.string.error_password_too_short)
            return
        }

        val validation = PasswordValidator.validate(newPassword)
        if (!validation.isValid) {
            _uiState.value = _uiState.value.copy(error = R.string.error_password_validation)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val result = authRepository.changePassword(user.id, oldPassword, newPassword)

            result.fold(
                onSuccess = {
                    authRepository.clearMustChangePassword(user.id)
                    _uiState.value = ChangePasswordUiState(success = true)
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = R.string.error_change_password
                    )
                }
            )
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
