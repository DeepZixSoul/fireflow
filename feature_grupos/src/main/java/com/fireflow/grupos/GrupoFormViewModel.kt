package com.fireflow.grupos

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.domain.repository.PressureGroupRepository
import com.fireflow.domain.model.PressureGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class GrupoFormState(
    val brand: String = "",
    val model: String = "",
    val serialNumber: String = "",
    val pumpNumber: String = "",
    val manufacturer: String = "",
    val power: String = "",
    val installationDate: String = "",
    val maintenanceDate: String = "",
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GrupoFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: PressureGroupRepository
) : ViewModel() {

    private val clientId: Long = savedStateHandle.get<String>("clientId")?.toLongOrNull() ?: -1L
    private val groupId: Long? = savedStateHandle.get<String>("groupId")?.toLongOrNull()

    private val _uiState = MutableStateFlow(GrupoFormState())
    val uiState: StateFlow<GrupoFormState> = _uiState.asStateFlow()

    init {
        if (groupId != null) {
            loadGroup()
        }
    }

    private fun loadGroup() {
        viewModelScope.launch {
            groupRepository.getGroupById(groupId!!).fold(
                onSuccess = { group ->
                    _uiState.value = _uiState.value.copy(
                        brand = group.brand,
                        model = group.model,
                        serialNumber = group.serialNumber,
                        pumpNumber = group.pumpNumber,
                        manufacturer = group.manufacturer,
                        power = group.power,
                        installationDate = group.installationDate?.toString() ?: "",
                        maintenanceDate = group.maintenanceDate?.toString() ?: ""
                    )
                },
                onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
            )
        }
    }

    fun onBrandChange(value: String) { _uiState.value = _uiState.value.copy(brand = value) }
    fun onModelChange(value: String) { _uiState.value = _uiState.value.copy(model = value) }
    fun onSerialNumberChange(value: String) { _uiState.value = _uiState.value.copy(serialNumber = value) }
    fun onPumpNumberChange(value: String) { _uiState.value = _uiState.value.copy(pumpNumber = value) }
    fun onManufacturerChange(value: String) { _uiState.value = _uiState.value.copy(manufacturer = value) }
    fun onPowerChange(value: String) { _uiState.value = _uiState.value.copy(power = value) }
    fun onInstallationDateChange(value: String) { _uiState.value = _uiState.value.copy(installationDate = value) }
    fun onMaintenanceDateChange(value: String) { _uiState.value = _uiState.value.copy(maintenanceDate = value) }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val group = PressureGroup(
                id = groupId ?: 0L,
                clientId = clientId,
                brand = _uiState.value.brand,
                model = _uiState.value.model,
                serialNumber = _uiState.value.serialNumber,
                pumpNumber = _uiState.value.pumpNumber,
                manufacturer = _uiState.value.manufacturer,
                power = _uiState.value.power,
                installationDate = _uiState.value.installationDate.toLongOrNull(),
                maintenanceDate = _uiState.value.maintenanceDate.toLongOrNull(),
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                isActive = true
            )

            groupRepository.saveGroup(group).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, error = it.message) }
            )
        }
    }
}
