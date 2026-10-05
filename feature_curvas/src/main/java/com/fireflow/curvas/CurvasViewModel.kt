package com.fireflow.curvas

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.domain.repository.MotorRepository
import com.fireflow.domain.repository.PressureMeasurementRepository
import com.fireflow.domain.repository.RevisionRepository
import com.fireflow.domain.model.Motor
import com.fireflow.domain.model.PressureMeasurement
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@Immutable
data class CurvasUiState(
    val motors: List<Motor> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class CurvasViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val revisionRepository: RevisionRepository,
    private val motorRepository: MotorRepository,
    private val measurementRepository: PressureMeasurementRepository
) : ViewModel() {

    private val revisionId: Long = savedStateHandle.get<String>("revisionId")?.toLongOrNull() ?: -1L

    private val _uiState = MutableStateFlow(CurvasUiState())
    val uiState: StateFlow<CurvasUiState> = _uiState.asStateFlow()

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _availableYears = MutableStateFlow<List<Int>>(emptyList())
    val availableYears: StateFlow<List<Int>> = _availableYears.asStateFlow()

    private val _measurements = MutableStateFlow<Map<Long, PressureMeasurement?>>(emptyMap())
    val measurements: StateFlow<Map<Long, PressureMeasurement?>> = _measurements.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val revision = revisionRepository.getRevisionById(revisionId).getOrNull()
            if (revision == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }

            // Default to the revision's year
            val revisionYear = Calendar.getInstance().apply { timeInMillis = revision.date }.get(Calendar.YEAR)
            _selectedYear.value = revisionYear

            motorRepository.getMotorsByGroup(revision.groupId).collect { motors ->
                _uiState.value = _uiState.value.copy(motors = motors, isLoading = false)

                if (motors.isNotEmpty()) {
                    observeYears(motors.map { it.id })
                    loadMeasurements(motors.map { it.id }, _selectedYear.value)
                }
            }
        }
    }

    private fun observeYears(motorIds: List<Long>) {
        viewModelScope.launch {
            measurementRepository.getAvailableYearsForMotors(motorIds).collect { years ->
                _availableYears.value = years
            }
        }
    }

    fun onYearSelected(year: Int) {
        _selectedYear.value = year
        val motorIds = _uiState.value.motors.map { it.id }
        if (motorIds.isNotEmpty()) {
            loadMeasurements(motorIds, year)
        }
    }

    private fun loadMeasurements(motorIds: List<Long>, year: Int) {
        viewModelScope.launch {
            val measurementsList = measurementRepository.getByMotorsAndYear(motorIds, year)
            _measurements.value = motorIds.associateWith { id ->
                measurementsList.find { it.motorId == id }
            }
        }
    }
}
