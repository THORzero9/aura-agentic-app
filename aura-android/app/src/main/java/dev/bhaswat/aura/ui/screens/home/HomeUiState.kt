package dev.bhaswat.aura.ui.screens.home

data class HomeUiState(
    val topic: String = "",
    val hours: Float = 5f,
    val selectedStyle: String = "Video tutorials",
    val isLoading: Boolean = false
)
