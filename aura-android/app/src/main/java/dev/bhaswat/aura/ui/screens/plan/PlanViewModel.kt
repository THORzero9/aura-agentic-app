package dev.bhaswat.aura.ui.screens.plan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.bhaswat.aura.data.PlanRepository
import dev.bhaswat.aura.network.LearningPlanResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlanViewModel(application: Application) : AndroidViewModel(application) {

    private val planRepository = PlanRepository(application.applicationContext)

    // Our ViewModel will now hold the UI state for the screen
    private val _uiState = MutableStateFlow(PlanUiState())
    val uiState = _uiState.asStateFlow()

    private val _saveEvent = MutableSharedFlow<String>()
    val saveEvent = _saveEvent.asSharedFlow()

    fun onSaveClicked(plan: LearningPlanResponse) {
        // If we are already saving, do nothing. This prevents double-clicks.
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            // Immediately set the state to saving to disable the button
            _uiState.update { it.copy(isSaving = true) }

            val success = planRepository.savePlan(plan)
            if (success) {
                _saveEvent.emit("Plan saved successfully!")
                // Mark as permanently saved
                _uiState.update { it.copy(isSaved = true) }
            } else {
                _saveEvent.emit("Error: Could not save plan.")
            }

            // When done, set the saving state back to false
            _uiState.update { it.copy(isSaving = false) }
        }
    }
}