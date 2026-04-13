package com.spendsense.app.frontend.viewmodels

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(
            email = email,
            username = email.substringBefore("@")
        )
    }

    fun updatePassword(password: String) {
        _uiState.value = _uiState.value.copy(password = password)
    }

    fun updateProfession(profession: String) {
        _uiState.value = _uiState.value.copy(selectedProfession = profession)
    }

    fun updateUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun updateBudget(budget: String) {
        _uiState.value = _uiState.value.copy(budget = budget)
    }

    fun updateProgress(progress: Float) {
        _uiState.value = _uiState.value.copy(currentProgress = progress)
    }
}

data class OnboardingUiState(
    val email: String = "",
    val password: String = "",
    val selectedProfession: String = "",
    val username: String = "",
    val budget: String = "",
    val currentProgress: Float = 0.33f
)
