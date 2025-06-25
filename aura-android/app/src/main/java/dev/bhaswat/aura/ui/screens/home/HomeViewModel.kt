package dev.bhaswat.aura.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bhaswat.aura.data.PlanRepository
import dev.bhaswat.aura.network.LearningRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val planRepository = PlanRepository()

    //called when the user types in the topic text field

    fun onTopicChange(newTopic: String) {
        _uiState.update { currentState ->
            currentState.copy(topic = newTopic)
        }

    }

    //called when the user moves the slider to a new value

    fun onHoursChange(newHours: Float) {
        _uiState.update { currentState ->
            currentState.copy(hours = newHours)
        }
    }

    //called when the user selects a learning style chip

    fun onStyleChange(newStyle: String) {
        _uiState.update { currentState ->
            currentState.copy(selectedStyle = newStyle)
        }
    }

    //Called when the main "Create my plan" button is clicked.

    fun onCreatePlanClick() {
        // Use viewModelScope to launch a coroutine. This is the safe, standard
        // way to do background work in a ViewModel.
        viewModelScope.launch {
            // Set the loading state to true
            _uiState.update { currentState ->
                currentState.copy(isLoading = true)
            }
            // Create the request object from the current UI state
            val request = LearningRequest(
                topic = uiState.value.topic ,
                hoursPerWeek = uiState.value.hours.toInt() ,
                preferredFormat = uiState.value.selectedStyle
            )

            val plan = planRepository.generateLearningPlan(request)

            // 4. Handle the response
            if (plan != null) {
                // Success! For now, we'll just print it.
                // Later, we'll navigate to the PlanScreen with this data.
                println("SUCCESS: Received plan -> ${plan.planTitle}")
            } else {
                // Error! For now, we'll just print it.
                // Later, we'll show an error message.
                println("ERROR: Failed to get a plan.")
            }

            // 5. Set the loading state back to false
            _uiState.update { it.copy(isLoading = false) }
        }
    }
}

