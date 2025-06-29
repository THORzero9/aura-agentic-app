package dev.bhaswat.aura.ui.screens.saved

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.bhaswat.aura.data.PlanRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SavedPlansViewModel(application: Application) : AndroidViewModel(application) {

    private val planRepository = PlanRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(SavedPlansUiState())
    val uiState = _uiState.asStateFlow()

    // The init block is called when the ViewModel is first created.
    // This is the perfect place to load our initial data.
    init {
        loadSavedPlans()
    }

    fun loadSavedPlans() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val plans = planRepository.getSavedPlans()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    savedPlans = plans
                )
            }
        }
    }
}