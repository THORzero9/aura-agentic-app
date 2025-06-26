package dev.bhaswat.aura.ui

import androidx.lifecycle.ViewModel
import dev.bhaswat.aura.network.LearningPlanResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedViewModel : ViewModel() {
    private val _plan = MutableStateFlow<LearningPlanResponse?>(null)
    val plan: StateFlow<LearningPlanResponse?> = _plan.asStateFlow()

    fun setPlan(newPlan: LearningPlanResponse) {
        _plan.value = newPlan
    }
}