package com.igrupos.clientes

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igrupos.domain.repository.ClientRepository
import com.igrupos.domain.repository.MotorRepository
import com.igrupos.domain.repository.PressureGroupRepository
import com.igrupos.domain.repository.PressureMeasurementRepository
import com.igrupos.domain.repository.RevisionRepository
import com.igrupos.domain.model.Client
import com.igrupos.domain.model.EntityStatus
import com.igrupos.domain.util.StatusCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@Immutable
data class ClientesUiState(
    val searchQuery: String = "",
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false
)

@HiltViewModel
class ClientesViewModel @Inject constructor(
    private val clientRepository: ClientRepository,
    private val groupRepository: PressureGroupRepository,
    private val revisionRepository: RevisionRepository,
    private val motorRepository: MotorRepository,
    private val measurementRepository: PressureMeasurementRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")

    private val _uiState = MutableStateFlow(ClientesUiState())
    val uiState: StateFlow<ClientesUiState> = _uiState.asStateFlow()

    val clients: StateFlow<List<Client>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                clientRepository.getClientsFlow()
            } else {
                clientRepository.searchClientsFlow(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _clientStatuses = MutableStateFlow<Map<Long, EntityStatus>>(emptyMap())
    val clientStatuses: StateFlow<Map<Long, EntityStatus>> = _clientStatuses.asStateFlow()

    private var searchJob: Job? = null

    init {
        observeClientsForStatus()
    }

    private fun observeClientsForStatus() {
        viewModelScope.launch {
            clients.collect { clientList ->
                _uiState.value = _uiState.value.copy(isLoading = false)
                calculateStatuses(clientList)
            }
        }
    }

    private suspend fun calculateStatuses(clients: List<Client>) {
        val allGroups = groupRepository.getAllGroups()
        val allRevisions = revisionRepository.getAllRevisions()

        val groupsByClient = allGroups.groupBy { it.clientId }
        val revisionsByGroup = allRevisions.groupBy { it.groupId }

        val statuses = mutableMapOf<Long, EntityStatus>()

        for (client in clients) {
            val clientGroups = groupsByClient[client.id] ?: emptyList()

            if (clientGroups.isEmpty()) {
                statuses[client.id] = EntityStatus.GRAY
                continue
            }

            val groupStatuses = clientGroups.map { group ->
                val groupRevisions = revisionsByGroup[group.id] ?: emptyList()

                if (groupRevisions.isEmpty()) return@map EntityStatus.GRAY

                val latestRevision = groupRevisions.maxByOrNull { it.date } ?: return@map EntityStatus.GRAY

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

                StatusCalculator.simpleRevisionStatus(hasChecklist, allChecklistTrue, hasPressureData)
            }

            statuses[client.id] = StatusCalculator.clientStatus(groupStatuses)
        }

        _clientStatuses.value = statuses
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300)
            _searchQuery.value = query
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun deleteClient(clientId: Long) {
        viewModelScope.launch {
            clientRepository.deleteClient(clientId)
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        viewModelScope.launch {
            delay(500)
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}
