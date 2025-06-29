package dev.bhaswat.aura.ui.screens.auth

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data object Success : AuthUiState
    data class Error(val message: String) : AuthUiState
    data object NeedsOtpVerification : AuthUiState
}