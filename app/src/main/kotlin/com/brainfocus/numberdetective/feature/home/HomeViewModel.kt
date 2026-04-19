package com.brainfocus.numberdetective.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.brainfocus.numberdetective.data.storage.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val highScore: StateFlow<Int> = dataStoreManager.highScoreFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    val isSoundEnabled: StateFlow<Boolean> = dataStoreManager.isSoundEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    val isHelperModeEnabled: StateFlow<Boolean> = dataStoreManager.isHelperModeEnabledFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun toggleSound(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.toggleSound(enabled) }
    }

    fun toggleHelperMode(enabled: Boolean) {
        viewModelScope.launch { dataStoreManager.toggleHelperMode(enabled) }
    }
}
