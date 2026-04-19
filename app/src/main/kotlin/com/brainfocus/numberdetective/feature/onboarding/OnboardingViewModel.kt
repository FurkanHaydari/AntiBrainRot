package com.brainfocus.numberdetective.feature.onboarding

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
class OnboardingViewModel @Inject constructor(
    private val dataStoreManager: DataStoreManager
) : ViewModel() {

    val isFirstLaunch: StateFlow<Boolean?> = dataStoreManager.isFirstLaunchFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun completeOnboarding() {
        viewModelScope.launch {
            dataStoreManager.completeOnboarding()
        }
    }
}
