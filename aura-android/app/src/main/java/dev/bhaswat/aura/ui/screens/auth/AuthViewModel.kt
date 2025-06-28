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

    fun signUp(email: String, pass: String, confirmPass: String) {
        viewModelScope.launch {
            if (pass != confirmPass) {
                _uiState.update { AuthUiState.Error("Passwords do not match.") }
                return@launch
            }
            if (pass.length < 8) {
                _uiState.update { AuthUiState.Error("Password must be at least 8 characters long.") }
                return@launch
            }

            _uiState.update { AuthUiState.Loading }
            val createdUser = authRepository.createUser(email, pass)

            if (createdUser != null) {
                // Step 2 (The Fix): LOG IN the new user immediately to create a session.
                val session = authRepository.createSession(email, pass)

                if (session != null) {
                    // Step 3: NOW that we have a session, send the verification email.
                    val verificationSent = authRepository.requestEmailVerification()
                    if (verificationSent) {
                        // Go to the screen that says "Please verify your email".
                        _uiState.update { AuthUiState.SignUpSuccessPendingVerification }
                    } else {
                        // This error is unlikely but good to have.
                        _uiState.update { AuthUiState.Error("Account created, but failed to send verification email.") }
                    }
                } else {
                    // This error means the account was created but the immediate login failed.
                    _uiState.update { AuthUiState.Error("Account created, but failed to log in.") }
                }
            } else {
                // This error usually means the user's email already exists.
                _uiState.update { AuthUiState.Error("Sign up failed. User may already exist.") }
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
    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading } // Show loading while resending
            val success = authRepository.resendEmailVerification()
            if (success) {
                _uiState.update { AuthUiState.SignUpSuccessPendingVerification } // Stay on the same screen, maybe show a success toast in UI
                // You might want a different state or a SharedFlow for "resend success" message
            } else {
                _uiState.update { AuthUiState.Error("Failed to resend verification email. Please try again.") }
            }
        }
    }

    fun resetState() {
        _uiState.update { AuthUiState.Idle }
    }
}