package com.igrupos.revisiones

import androidx.compose.runtime.Immutable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igrupos.domain.repository.ChecklistRepository
import com.igrupos.domain.repository.RevisionRepository
import com.igrupos.domain.model.ChecklistItem
import com.igrupos.domain.model.Revision
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class RevisionFormState(
    val date: Long = System.currentTimeMillis(),
    val technicianName: String = "",
    val notes: String = "",
    val checklistResults: Map<String, Boolean> = emptyMap(),
    val isCopyFromPrevious: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class RevisionFormViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val revisionRepository: RevisionRepository,
    private val checklistRepository: ChecklistRepository
) : ViewModel() {

    private val groupId: Long = savedStateHandle.get<String>("groupId")?.toLongOrNull() ?: -1L
    private val revisionId: Long? = savedStateHandle.get<String>("revisionId")?.toLongOrNull()

    private val _uiState = MutableStateFlow(RevisionFormState())
    val uiState: StateFlow<RevisionFormState> = _uiState.asStateFlow()

    val checklistItems: StateFlow<List<ChecklistItem>> =
        checklistRepository.getChecklistItemsFlow()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var previousChecklistResults: Map<String, Boolean> = emptyMap()

    init {
        viewModelScope.launch {
            if (revisionId != null) {
                loadRevision()
            }
            loadChecklistDefaults()
        }
    }

    private suspend fun loadRevision() {
        revisionRepository.getRevisionById(revisionId!!).fold(
            onSuccess = { revision ->
                _uiState.value = _uiState.value.copy(
                    date = revision.date,
                    technicianName = revision.technicianName,
                    notes = revision.notes,
                    checklistResults = revision.checklistResults
                )
                previousChecklistResults = revision.checklistResults
            },
            onFailure = { _uiState.value = _uiState.value.copy(error = it.message) }
        )
    }

    private suspend fun loadChecklistDefaults() {
        if (revisionId != null) return

        val previous = revisionRepository.getLastRevisionByGroup(groupId).getOrNull()
        val items = checklistItems.value

        val defaults = if (previous != null && _uiState.value.isCopyFromPrevious) {
            previous.checklistResults
        } else {
            items.associate { it.label to false }
        }

        _uiState.value = _uiState.value.copy(checklistResults = defaults)
    }

    fun toggleCopyFromPrevious() {
        _uiState.value = _uiState.value.copy(isCopyFromPrevious = !_uiState.value.isCopyFromPrevious)
        viewModelScope.launch { loadChecklistDefaults() }
    }

    fun onDateChange(value: Long) {
        _uiState.value = _uiState.value.copy(date = value)
    }

    fun onTechnicianNameChange(value: String) {
        _uiState.value = _uiState.value.copy(technicianName = value)
    }

    fun onNotesChange(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun onChecklistToggle(label: String, checked: Boolean) {
        val updated = _uiState.value.checklistResults.toMutableMap()
        updated[label] = checked
        _uiState.value = _uiState.value.copy(checklistResults = updated)
    }

    fun save() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val revision = Revision(
                id = revisionId ?: 0L,
                groupId = groupId,
                date = _uiState.value.date,
                technicianName = _uiState.value.technicianName,
                notes = _uiState.value.notes,
                checklistResults = _uiState.value.checklistResults,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            revisionRepository.saveRevision(revision).fold(
                onSuccess = { _uiState.value = _uiState.value.copy(isSaving = false, isSaved = true) },
                onFailure = { _uiState.value = _uiState.value.copy(isSaving = false, error = it.message) }
            )
        }
    }
}
