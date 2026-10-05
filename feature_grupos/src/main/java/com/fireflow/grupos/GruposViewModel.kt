package com.fireflow.grupos

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.domain.repository.MotorRepository
import com.fireflow.domain.repository.PressureGroupRepository
import com.fireflow.domain.repository.PressureMeasurementRepository
import com.fireflow.domain.repository.RevisionRepository
import com.fireflow.domain.model.EntityStatus
import com.fireflow.domain.model.MotorType
import com.fireflow.domain.model.PressureGroup
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.fireflow.domain.util.StatusCalculator
import javax.inject.Inject

@Immutable
data class GroupListItem(
    val group: PressureGroup,
    val motorTypes: List<MotorType>,
    val status: EntityStatus = EntityStatus.GRAY
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class GruposViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val groupRepository: PressureGroupRepository,
    private val motorRepository: MotorRepository,
    private val revisionRepository: RevisionRepository,
    private val measurementRepository: PressureMeasurementRepository
) : ViewModel() {

    private val clientId: Long = savedStateHandle.get<String>("clientId")?.toLongOrNull() ?: -1L

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val groupItems: StateFlow<List<GroupListItem>> = groupRepository.getGroupsByClientFlow(clientId)
        .flatMapLatest { groups ->
            if (groups.isEmpty()) {
                kotlinx.coroutines.flow.flowOf(emptyList())
            } else {
                val motorFlows = groups.map { group ->
                    motorRepository.getMotorsByGroup(group.id).map { motors ->
                        group.id to motors.map { it.motorType }.distinct()
                    }
                }
                combine(motorFlows) { results ->
                    val typeMap = results.toMap()
                    groups.map { group ->
                        GroupListItem(group, typeMap[group.id] ?: emptyList())
                    }
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        observeGroupsForStatus()
    }

    private fun observeGroupsForStatus() {
        viewModelScope.launch {
            groupItems.collect { items ->
                val updatedItems = items.map { item ->
                    val status = calculateGroupStatus(item.group)
                    item.copy(status = status)
                }
                // Update via a re-emission by using a separate flow
                _groupItemsWithStatus.value = updatedItems
            }
        }
    }

    private val _groupItemsWithStatus = MutableStateFlow<List<GroupListItem>>(emptyList())
    val groupItemsWithStatus: StateFlow<List<GroupListItem>> = _groupItemsWithStatus.asStateFlow()

    private suspend fun calculateGroupStatus(group: PressureGroup): EntityStatus {
        val revisions = revisionRepository.getRevisionsByGroupOneShot(group.id)
        if (revisions.isEmpty()) return EntityStatus.GRAY

        val motors = motorRepository.getMotorsByGroupOneShot(group.id)

        val revisionStatuses = revisions.map { revision ->
            val hasChecklist = revision.checklistResults.isNotEmpty()
            val allChecklistTrue = hasChecklist && revision.checklistResults.values.all { it }

            val hasPressureData = motors.any { motor ->
                val measurement = measurementRepository.getByMotorAndYear(motor.id, revision.date.toInt())
                measurement != null && (
                    measurement.pressureAt0 != null ||
                    measurement.pressureAt50 != null ||
                    measurement.pressureAt100 != null ||
                    measurement.pressureAt140 != null
                )
            }

            StatusCalculator.simpleRevisionStatus(hasChecklist, allChecklistTrue, hasPressureData)
        }

        return StatusCalculator.simpleGroupStatus(revisionStatuses)
    }

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            delay(500)
            _isRefreshing.value = false
        }
    }
}
