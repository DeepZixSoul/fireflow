package com.fireflow.grupos

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.domain.repository.MotorRepository
import com.fireflow.domain.repository.PressureGroupRepository
import com.fireflow.domain.repository.PressureMeasurementRepository
import com.fireflow.domain.repository.RevisionRepository
import com.fireflow.domain.model.EntityStatus
import com.fireflow.domain.model.Motor
import com.fireflow.domain.model.PressureGroup
import com.fireflow.domain.model.PressureMeasurement
import com.fireflow.domain.model.Revision
import com.fireflow.domain.util.StatusCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.Calendar
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class GrupoDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: PressureGroupRepository,
    private val revisionRepository: RevisionRepository,
    private val motorRepository: MotorRepository,
    private val measurementRepository: PressureMeasurementRepository
) : ViewModel() {

    private val groupId: Long = savedStateHandle.get<String>("groupId")?.toLongOrNull() ?: -1L

    private val _group = MutableStateFlow<PressureGroup?>(null)
    val group: StateFlow<PressureGroup?> = _group.asStateFlow()

    val revisions: StateFlow<List<Revision>> = revisionRepository.getRevisionsByGroupFlow(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val motors: StateFlow<List<Motor>> = motorRepository.getMotorsByGroup(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedYear = MutableStateFlow(Calendar.getInstance().get(Calendar.YEAR))
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    private val _isYearExpanded = MutableStateFlow(true)
    val isYearExpanded: StateFlow<Boolean> = _isYearExpanded.asStateFlow()

    private val _availableYears = MutableStateFlow<List<Int>>(emptyList())
    val availableYears: StateFlow<List<Int>> = _availableYears.asStateFlow()

    private val _motorMeasurements = MutableStateFlow<Map<Long, PressureMeasurement?>>(emptyMap())
    val motorMeasurements: StateFlow<Map<Long, PressureMeasurement?>> = _motorMeasurements.asStateFlow()

    private val _revisionStatuses = MutableStateFlow<Map<Long, EntityStatus>>(emptyMap())
    val revisionStatuses: StateFlow<Map<Long, EntityStatus>> = _revisionStatuses.asStateFlow()

    init {
        loadGroup()
        viewModelScope.launch {
            motors.flatMapLatest { motorList ->
                if (motorList.isEmpty()) flowOf(emptyList())
                else measurementRepository.getAvailableYearsForMotors(motorList.map { it.id })
            }.collect { years ->
                _availableYears.value = years
            }
        }
        viewModelScope.launch {
            motors.collect { motorList ->
                if (motorList.isNotEmpty()) {
                    loadMeasurementsForYear(_selectedYear.value)
                }
            }
        }
        observeRevisionsForStatus()
    }

    private fun observeRevisionsForStatus() {
        viewModelScope.launch {
            revisions.collect { revisionList ->
                calculateRevisionStatuses(revisionList)
            }
        }
    }

    private suspend fun calculateRevisionStatuses(revisions: List<Revision>) {
        val motorList = motors.first()
        val statuses = mutableMapOf<Long, EntityStatus>()

        for (revision in revisions) {
            val hasChecklist = revision.checklistResults.isNotEmpty()
            val allChecklistTrue = hasChecklist && revision.checklistResults.values.all { it }

            val revisionYear = Calendar.getInstance().apply { timeInMillis = revision.date }.get(Calendar.YEAR)
            val hasPressureData = motorList.any { motor ->
                val measurement = measurementRepository.getByMotorAndYear(motor.id, revisionYear)
                measurement != null && (
                    measurement.pressureAt0 != null ||
                    measurement.pressureAt50 != null ||
                    measurement.pressureAt100 != null ||
                    measurement.pressureAt140 != null
                )
            }

            statuses[revision.id] = StatusCalculator.simpleRevisionStatus(hasChecklist, allChecklistTrue, hasPressureData)
        }

        _revisionStatuses.value = statuses
    }

    private fun loadGroup() {
        viewModelScope.launch {
            groupRepository.getGroupById(groupId).fold(
                onSuccess = { _group.value = it },
                onFailure = { _group.value = null }
            )
        }
    }

    fun onYearSelected(year: Int) {
        if (year == _selectedYear.value) {
            _isYearExpanded.value = !_isYearExpanded.value
        } else {
            _selectedYear.value = year
            _isYearExpanded.value = true
            loadMeasurementsForYear(year)
        }
    }

    private fun loadMeasurementsForYear(year: Int) {
        viewModelScope.launch {
            val motorList = motors.first()
            if (motorList.isEmpty()) {
                _motorMeasurements.value = emptyMap()
                return@launch
            }
            val measurementsList = measurementRepository.getByMotorsAndYear(motorList.map { it.id }, year)
            val measurements = motorList.associate { motor ->
                motor.id to measurementsList.find { it.motorId == motor.id }
            }
            _motorMeasurements.value = measurements
        }
    }

    fun getMeasurementForMotor(motorId: Long): PressureMeasurement? {
        return _motorMeasurements.value[motorId]
    }

    fun createNextYearMeasurement(motorId: Long) {
        viewModelScope.launch {
            val maxYear = _availableYears.value.maxOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
            val newYear = maxYear + 1
            val measurement = PressureMeasurement(
                id = 0L,
                motorId = motorId,
                year = newYear,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            measurementRepository.save(measurement)
            onYearSelected(newYear)
        }
    }

    fun onPressureChange(motorId: Long, field: String, value: String) {
        val current = _motorMeasurements.value.toMutableMap()
        val existing = current[motorId]
        val now = System.currentTimeMillis()
        val parsedValue = value.replace(',', '.').toDoubleOrNull()

        val measurement = if (existing != null) {
            when (field) {
                "0" -> existing.copy(pressureAt0 = parsedValue, updatedAt = now)
                "50" -> existing.copy(pressureAt50 = parsedValue, updatedAt = now)
                "100" -> existing.copy(pressureAt100 = parsedValue, updatedAt = now)
                "140" -> existing.copy(pressureAt140 = parsedValue, updatedAt = now)
                else -> existing
            }
        } else {
            PressureMeasurement(
                id = 0L,
                motorId = motorId,
                year = _selectedYear.value,
                pressureAt0 = parsedValue.takeIf { field == "0" },
                pressureAt50 = parsedValue.takeIf { field == "50" },
                pressureAt100 = parsedValue.takeIf { field == "100" },
                pressureAt140 = parsedValue.takeIf { field == "140" },
                createdAt = now,
                updatedAt = now
            )
        }

        current[motorId] = measurement
        _motorMeasurements.value = current

        viewModelScope.launch {
            measurementRepository.save(measurement)
        }
    }

    fun deleteGroup(onDeleted: () -> Unit) {
        viewModelScope.launch {
            groupRepository.deleteGroup(groupId)
            onDeleted()
        }
    }
}
