package com.fireflow.clientes

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.core.util.GeocoderUtil
import com.fireflow.domain.repository.ClientRepository
import com.fireflow.domain.repository.MotorRepository
import com.fireflow.domain.repository.PressureGroupRepository
import com.fireflow.domain.repository.PressureMeasurementRepository
import com.fireflow.domain.repository.RevisionRepository
import com.fireflow.domain.model.Client
import com.fireflow.domain.model.EntityStatus
import com.fireflow.domain.model.PressureGroup
import com.fireflow.domain.util.StatusCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ClienteDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
    private val clientRepository: ClientRepository,
    private val groupRepository: PressureGroupRepository,
    private val revisionRepository: RevisionRepository,
    private val motorRepository: MotorRepository,
    private val measurementRepository: PressureMeasurementRepository
) : ViewModel() {

    private val clientId: Long = savedStateHandle.get<String>("clientId")?.toLongOrNull() ?: -1L

    private val _client = MutableStateFlow<Client?>(null)
    val client: StateFlow<Client?> = _client.asStateFlow()

    private val _installationAddress = MutableStateFlow<String?>(null)
    val installationAddress: StateFlow<String?> = _installationAddress.asStateFlow()

    private val _isRefreshingAddress = MutableStateFlow(false)
    val isRefreshingAddress: StateFlow<Boolean> = _isRefreshingAddress.asStateFlow()

    val groups: StateFlow<List<PressureGroup>> = groupRepository.getGroupsByClientFlow(clientId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _groupStatuses = MutableStateFlow<Map<Long, EntityStatus>>(emptyMap())
    val groupStatuses: StateFlow<Map<Long, EntityStatus>> = _groupStatuses.asStateFlow()

    init {
        loadClient()
        observeGroupsForStatus()
    }

    private fun observeGroupsForStatus() {
        viewModelScope.launch {
            groups.collect { groupList ->
                calculateGroupStatuses(groupList)
            }
        }
    }

    private suspend fun calculateGroupStatuses(groups: List<PressureGroup>) {
        val allRevisions = revisionRepository.getAllRevisions()
        val revisionsByGroup = allRevisions.groupBy { it.groupId }

        val statuses = mutableMapOf<Long, EntityStatus>()

        for (group in groups) {
            val groupRevisions = revisionsByGroup[group.id] ?: emptyList()

            if (groupRevisions.isEmpty()) {
                statuses[group.id] = EntityStatus.YELLOW
                continue
            }

            val latestRevision = groupRevisions.maxByOrNull { it.date } ?: continue
            val hasChecklist = latestRevision.checklistResults.isNotEmpty()
            val allChecklistTrue = hasChecklist && latestRevision.checklistResults.values.all { it }

            val revisionYear = Calendar.getInstance().apply { timeInMillis = latestRevision.date }.get(Calendar.YEAR)
            val motors = motorRepository.getMotorsByGroupOneShot(group.id)
            val hasPressureData = motors.any { motor ->
                val measurement = measurementRepository.getByMotorAndYear(motor.id, revisionYear)
                measurement != null && (
                    measurement.pressureAt0 != null ||
                    measurement.pressureAt50 != null ||
                    measurement.pressureAt100 != null ||
                    measurement.pressureAt140 != null
                )
            }

            statuses[group.id] = StatusCalculator.simpleRevisionStatus(hasChecklist, allChecklistTrue, hasPressureData)
        }

        _groupStatuses.value = statuses
    }

    private fun loadClient() {
        viewModelScope.launch {
            clientRepository.getClientById(clientId).fold(
                onSuccess = { client ->
                    _client.value = client
                    resolveInstallationAddress(client)
                },
                onFailure = { _client.value = null }
            )
        }
    }

    fun refreshInstallationAddress() {
        val client = _client.value ?: return
        resolveInstallationAddress(client)
    }

    fun deleteClient(onDeleted: () -> Unit) {
        viewModelScope.launch {
            clientRepository.deleteClient(clientId)
            onDeleted()
        }
    }

    private fun resolveInstallationAddress(client: Client) {
        val lat = client.latitude
        val lng = client.longitude
        if (lat == null || lng == null) {
            _installationAddress.value = null
            return
        }
        viewModelScope.launch {
            _isRefreshingAddress.value = true
            _installationAddress.value = GeocoderUtil.reverseGeocode(context, lat, lng)
            _isRefreshingAddress.value = false
        }
    }
}
