package dev.bhaswat.aura.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.bhaswat.aura.ui.SharedViewModel

class HomeViewModelFactory(private val sharedViewModel: SharedViewModel) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(sharedViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}