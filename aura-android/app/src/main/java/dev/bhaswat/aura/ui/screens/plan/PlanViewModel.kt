package dev.bhaswat.aura.ui.screens.plan

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.bhaswat.aura.data.PlanRepository
import dev.bhaswat.aura.network.LearningPlanResponse
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class PlanViewModel(application: Application) : AndroidViewModel(application) {

    private val planRepository = PlanRepository(application.applicationContext)

    // A SharedFlow to send one-time events (like "Save Complete!") to the UI
    private val _saveEvent = MutableSharedFlow<String>()
    val saveEvent = _saveEvent.asSharedFlow()

    fun onSaveClicked(plan: LearningPlanResponse) {
        viewModelScope.launch {
            val success = planRepository.savePlan(plan)
            if (success) {
                _saveEvent.emit("Plan saved successfully!")
            } else {
                _saveEvent.emit("Error: Could not save plan.")
            }
        }
    }
}