package com.igrupos.configuracion

import android.content.Context
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igrupos.domain.repository.AuthRepository
import com.igrupos.domain.repository.SyncManagerRepository
import com.igrupos.domain.repository.SyncSchedulerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class SettingsUiState(
    val syncEnabled: Boolean = true,
    val serverUrl: String = "",
    val authToken: String = "",
    val lastSyncTime: Long = 0L,
    val isSyncing: Boolean = false,
    val syncError: String? = null,
    val configSaved: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManagerRepository,
    private val syncScheduler: SyncSchedulerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadConfig()
    }

    private fun loadConfig() {
        viewModelScope.launch {
            val url = syncManager.getServerUrl()
            val token = syncManager.getAuthToken()
            val enabled = syncManager.isSyncEnabled.stateIn(viewModelScope).value
            _uiState.value = _uiState.value.copy(
                serverUrl = url,
                authToken = token,
                syncEnabled = enabled
            )

            syncManager.lastSyncTime.collect { time ->
                _uiState.value = _uiState.value.copy(lastSyncTime = time)
            }
        }
    }

    fun toggleSync() {
        val enabled = !_uiState.value.syncEnabled
        _uiState.value = _uiState.value.copy(syncEnabled = enabled)
        viewModelScope.launch {
            syncManager.setSyncEnabled(enabled)
        }
        if (enabled) {
            syncScheduler.schedule()
        } else {
            syncScheduler.cancel()
        }
    }

    fun updateServerUrl(url: String) {
        _uiState.value = _uiState.value.copy(serverUrl = url, configSaved = false)
    }

    fun updateAuthToken(token: String) {
        _uiState.value = _uiState.value.copy(authToken = token, configSaved = false)
    }

    fun saveServerConfig() {
        viewModelScope.launch {
            syncManager.saveServerConfig(_uiState.value.serverUrl, _uiState.value.authToken)
            _uiState.value = _uiState.value.copy(configSaved = true, syncError = null)
        }
    }

    fun syncNow() {
        if (_uiState.value.isSyncing) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSyncing = true, syncError = null)
            val result = syncManager.performSync()
            _uiState.value = if (result.isSuccess) {
                _uiState.value.copy(isSyncing = false)
            } else {
                _uiState.value.copy(
                    isSyncing = false,
                    syncError = result.exceptionOrNull()?.message ?: "Error desconocido"
                )
            }
        }
    }

    fun clearSyncError() {
        _uiState.value = _uiState.value.copy(syncError = null)
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
