package dev.bhaswat.aura.ui.screens.auth

import android.app.Application
import androidx.activity.ComponentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import dev.bhaswat.aura.data.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val authRepository = AuthRepository(application.applicationContext)

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState = _uiState.asStateFlow()

    fun checkCurrentUser() {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            val user = authRepository.getCurrentUser()
            if (user != null) {
                // If a user is found, go to the Success state
                _uiState.update { AuthUiState.Success }
            } else {
                // If no user is found, go back to the Idle state (which shows the login form)
                _uiState.update { AuthUiState.Idle }
            }
        }
    }

    fun signUp(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            val user = authRepository.createUser(email, pass)
            if (user != null) {
                // If sign up is successful, try to log them in immediately
                login(email, pass)
            } else {
                _uiState.update { AuthUiState.Error("Sign up failed. Please try again.") }
            }
        }
    }

    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            val user = authRepository.createSession(email, pass)
            if (user != null) {
                _uiState.update { AuthUiState.Success }
            } else {
                _uiState.update { AuthUiState.Error("Login failed. Please check your credentials.") }
            }
        }
    }

    fun signInWithGoogle(activity: ComponentActivity) {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            val user = authRepository.signInWithGoogle(activity)
            if (user != null) {
                _uiState.update { AuthUiState.Success }
            } else {
                _uiState.update { AuthUiState.Error("Google Sign-In failed.") }
            }
        }
    }

    fun resetState() {
        _uiState.update { AuthUiState.Idle }
    }
}