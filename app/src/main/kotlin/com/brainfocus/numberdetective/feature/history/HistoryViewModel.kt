package com.brainfocus.numberdetective.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.data.storage.DataStoreManager
import com.brainfocus.numberdetective.data.storage.GameSession
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val history: StateFlow<List<GameSession>> = dataStoreManager.historyFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
