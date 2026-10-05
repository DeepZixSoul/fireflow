package com.igrupos.revisiones

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.igrupos.domain.repository.RevisionRepository
import com.igrupos.domain.model.Revision
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RevisionDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val revisionRepository: RevisionRepository
) : ViewModel() {

    private val revisionId: Long = savedStateHandle.get<String>("revisionId")?.toLongOrNull() ?: -1L

    private val _revision = MutableStateFlow<Revision?>(null)
    val revision: StateFlow<Revision?> = _revision.asStateFlow()

    init {
        loadRevision()
    }

    private fun loadRevision() {
        viewModelScope.launch {
            revisionRepository.getRevisionById(revisionId).fold(
                onSuccess = { _revision.value = it },
                onFailure = { _revision.value = null }
            )
        }
    }

    fun deleteRevision(onDeleted: () -> Unit) {
        viewModelScope.launch {
            revisionRepository.deleteRevision(revisionId)
            onDeleted()
        }
    }
}
