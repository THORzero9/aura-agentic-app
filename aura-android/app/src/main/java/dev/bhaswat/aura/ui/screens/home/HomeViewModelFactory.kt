package dev.bhaswat.aura.ui.screens.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.bhaswat.aura.ui.SharedViewModel

class HomeViewModelFactory(
    private val application: Application , // It now holds the application context
    private val sharedViewModel: SharedViewModel
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Pass both dependencies to the constructor
            return HomeViewModel(application, sharedViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}