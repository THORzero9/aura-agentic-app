package dev.bhaswat.aura.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.bhaswat.aura.data.PlanRepository
import dev.bhaswat.aura.network.LearningRequest
import dev.bhaswat.aura.ui.SharedViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val sharedViewModel: SharedViewModel) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    private val planRepository = PlanRepository()

    //called when the user types in the topic text field

    fun onTopicChange(newTopic: String) {
        _uiState.update {
            it.copy(topic = newTopic)
        }

    }

    //called when the user moves the slider to a new value

    fun onHoursChange(newHours: Float) {
        _uiState.update {
            it.copy(hours = newHours)
        }
    }

    //called when the user selects a learning style chip

    fun onStyleChange(newStyle: String) {
        _uiState.update {
            it.copy(selectedStyle = newStyle)
        }
    }

    //Called when the main "Create my plan" button is clicked.

    fun onCreatePlanClick() {
        // Use viewModelScope to launch a coroutine. This is the safe, standard
        // way to do background work in a ViewModel.
        viewModelScope.launch {
            // Set the loading state to true
            _uiState.update {
                it.copy(isLoading = true)
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
                sharedViewModel.setPlan(plan)
                _navigationEvent.emit(Unit)
            } else {
                // UPDATE THIS PART: Instead of just printing, post an error message.
                _errorState.value = "Failed to generate plan. Please try again."
            }
            _uiState.update { it.copy(isLoading = false) }
        }
    }
    fun onErrorShown() {
        _errorState.value = null
    }
}

