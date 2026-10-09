package com.fireflow.app.navigation

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Resolves the initial route of the app:
 * - no users yet → first-run setup (create administrator)
 * - users exist → login
 *
 * Emits `null` while the decision is pending so the UI can show a loading state.
 */
@Immutable
@HiltViewModel
class StartDestinationViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination.asStateFlow()

    init {
        viewModelScope.launch {
            _startDestination.value = if (authRepository.hasUsers()) {
                Routes.LOGIN
            } else {
                Routes.SETUP
            }
        }
    }
}
