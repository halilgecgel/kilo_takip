package com.kilotakip.app.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kilotakip.app.data.local.entity.WeightEntryEntity
import com.kilotakip.app.data.repository.WeightEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val repository: WeightEntryRepository
) : ViewModel() {

    val entries: StateFlow<List<WeightEntryEntity>> = repository.observeAll()
        .map { it.sortedByDescending { entry -> entry.recordedAt } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun delete(clientUuid: String) {
        viewModelScope.launch { repository.deleteEntry(clientUuid) }
    }
}
