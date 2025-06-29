package dev.bhaswat.aura.ui.screens.saved

import dev.bhaswat.aura.network.LearningPlanResponse

data class SavedPlansUiState(
    val isLoading: Boolean = false ,
    val savedPlans: List<LearningPlanResponse> = emptyList() ,
    val error: String? = null
)