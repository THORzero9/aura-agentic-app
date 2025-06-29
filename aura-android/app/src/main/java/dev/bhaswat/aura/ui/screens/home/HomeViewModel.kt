package dev.bhaswat.aura.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.bhaswat.aura.data.AuthRepository
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

class HomeViewModel(
    application: Application , // It now receives the application context
    private val sharedViewModel: SharedViewModel
) : AndroidViewModel(application) { // It now extends AndroidViewModel

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<Unit>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    // 2. UPDATE THE REPOSITORY INSTANTIATION
    // We now have the context to pass to the repository
    private val planRepository = PlanRepository(application.applicationContext)


    // The rest of the functions (onTopicChange, onCreatePlanClick, etc.)
    // are exactly the same. No changes needed there.
    fun onTopicChange(newTopic: String) {
        _uiState.update { it.copy(topic = newTopic) }
    }

    fun onHoursChange(newHours: Float) {
        _uiState.update { it.copy(hours = newHours) }
    }

    fun onStyleChange(newStyle: String) {
        _uiState.update { it.copy(selectedStyle = newStyle) }
    }

    //Called when the main "Create my plan" button is clicked.
    private val authRepository = AuthRepository(getApplication())
    fun onCreatePlanClick() {
        // Use viewModelScope to launch a coroutine. This is the safe, standard
        // way to do background work in a ViewModel.
        viewModelScope.launch {
            // Set the loading state to true
            _uiState.update {
                it.copy(isLoading = true)
            }
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                // If there's no user, show an error and stop.
                _errorState.value = "Error: You must be logged in to create a plan."
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            // Create the request object from the current UI state
            val request = LearningRequest(
                topic = uiState.value.topic ,
                hoursPerWeek = uiState.value.hours.toInt() ,
                preferredFormat = uiState.value.selectedStyle ,
                userId = currentUser.id
            )

            val plan = planRepository.generateLearningPlan(request)

            // 4. Handle the response
            if (plan != null) {
                sharedViewModel.setPlan(plan)
                _uiState.update { it.copy(isLoading = false) }

                _navigationEvent.emit(Unit)
            } else {
                // Instead of just printing, we now post an error message
                _errorState.value = "Failed to generate plan. Please try again."
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ADD THIS FUNCTION to allow the UI to clear the error message after showing it
    fun onErrorShown() {
        _errorState.value = null
    }
}