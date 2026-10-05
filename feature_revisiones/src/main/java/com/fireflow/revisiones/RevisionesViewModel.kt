package com.fireflow.revisiones

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fireflow.domain.repository.RevisionRepository
import com.fireflow.domain.model.Revision
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RevisionesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val revisionRepository: RevisionRepository
) : ViewModel() {

    private val groupId: Long = savedStateHandle.get<String>("groupId")?.toLongOrNull() ?: -1L

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val revisions: StateFlow<List<Revision>> = revisionRepository.getRevisionsByGroupFlow(groupId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun refresh() {
        _isRefreshing.value = true
        viewModelScope.launch {
            delay(500)
            _isRefreshing.value = false
        }
    }
}
