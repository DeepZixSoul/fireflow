package com.igrupos.grupos

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igrupos.domain.repository.MotorRepository
import com.igrupos.domain.repository.PressureMeasurementRepository
import com.igrupos.domain.model.Motor
import com.igrupos.domain.model.MotorType
import com.igrupos.domain.model.PressureMeasurement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@Immutable
data class MotorFormEntry(
    val motorType: MotorType = MotorType.ELECTRIC,
    val nominalFlow: String = "",
    val manometricHeight: String = ""
)

@Immutable
data class MotorFormState(
    val motors: List<MotorFormEntry> = emptyList(),
    val motorIds: List<Long> = emptyList(),
    val motorCreatedAts: List<Long> = emptyList(),
    val availableYears: List<Int> = emptyList(),
    val selectedYear: Int = Calendar.getInstance().get(Calendar.YEAR),
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class MotorFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val motorRepository: MotorRepository,
    private val measurementRepository: PressureMeasurementRepository
) : ViewModel() {

    private val groupId: Long = savedStateHandle.get<String>("groupId")?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(MotorFormState())
    val uiState: StateFlow<MotorFormState> = _uiState.asStateFlow()

    init {
        loadMotors()
    }

    private fun loadMotors() {
        viewModelScope.launch {
            motorRepository.getMotorsByGroup(groupId).collect { motors ->
                val motorData = motors.map { motor ->
                    MotorFormEntry(
                        motorType = motor.motorType,
                        nominalFlow = motor.nominalFlow.toBigDecimal().stripTrailingZeros().toPlainString(),
                        manometricHeight = motor.manometricHeight.toBigDecimal().stripTrailingZeros().toPlainString()
                    )
                }

                val allYears = if (motors.isEmpty()) {
                    emptyList()
                } else {
                    measurementRepository.getAvailableYearsForMotors(motors.map { it.id }).first()
                }

                _uiState.value = _uiState.value.copy(
                    motors = motorData,
                    motorIds = motors.map { it.id },
                    motorCreatedAts = motors.map { it.createdAt },
                    availableYears = allYears
                )
            }
        }
    }

    fun onYearSelected(year: Int) {
        _uiState.value = _uiState.value.copy(selectedYear = year)
    }

    fun onNewYear() {
        viewModelScope.launch {
            val maxYear = _uiState.value.availableYears.maxOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            val newYear = maxYear + 1

            val motors = motorRepository.getMotorsByGroup(groupId).first()
            for (motor in motors) {
                val measurement = PressureMeasurement(
                    id = 0L,
                    motorId = motor.id,
                    year = newYear,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                measurementRepository.save(measurement)
            }

            _uiState.value = _uiState.value.copy(selectedYear = newYear)
            loadMotors()
        }
    }

    fun addMotor() {
        _uiState.value = _uiState.value.copy(
            motors = _uiState.value.motors + MotorFormEntry(),
            motorIds = _uiState.value.motorIds + 0L,
            motorCreatedAts = _uiState.value.motorCreatedAts + System.currentTimeMillis()
        )
    }

    fun removeMotor(index: Int) {
        val state = _uiState.value
        if (state.motors.size > 1) {
            _uiState.value = state.copy(
                motors = state.motors.toMutableList().apply { removeAt(index) },
                motorIds = state.motorIds.toMutableList().apply { removeAt(index) },
                motorCreatedAts = state.motorCreatedAts.toMutableList().apply { removeAt(index) }
            )
        }
    }

    fun onMotorTypeChange(index: Int, type: MotorType) {
        updateMotorEntry(index) { it.copy(motorType = type) }
    }

    fun onMotorNominalFlowChange(index: Int, value: String) {
        updateMotorEntry(index) { it.copy(nominalFlow = value) }
    }

    fun onMotorManometricHeightChange(index: Int, value: String) {
        updateMotorEntry(index) { it.copy(manometricHeight = value) }
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val state = _uiState.value

            for (i in state.motors.indices) {
                val entry = state.motors[i]
                val flow = entry.nominalFlow.toDoubleOrNull()
                val height = entry.manometricHeight.toDoubleOrNull()
                if (flow == null || height == null) continue

                val motor = Motor(
                    id = state.motorIds[i],
                    groupId = groupId,
                    motorType = entry.motorType,
                    nominalFlow = flow,
                    manometricHeight = height,
                    createdAt = state.motorCreatedAts[i].takeIf { it != 0L } ?: System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                if (motor.id != 0L) {
                    motorRepository.updateMotor(motor)
                } else {
                    motorRepository.saveMotor(motor)
                }
            }

            _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true)
        }
    }

    private fun updateMotorEntry(index: Int, transform: (MotorFormEntry) -> MotorFormEntry) {
        val motors = _uiState.value.motors.toMutableList()
        motors[index] = transform(motors[index])
        _uiState.value = _uiState.value.copy(motors = motors)
    }
}
