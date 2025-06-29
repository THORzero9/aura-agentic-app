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

    // We need to temporarily store the user's email between the sign-up and OTP screens.
    private var emailForVerification: String? = null
    private var userIdForVerification: String? = null
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

    fun signUp(name: String, email: String, pass: String, confirmPass: String) {
        viewModelScope.launch {
            if (pass != confirmPass) {
                _uiState.update { AuthUiState.Error("Passwords do not match.") }
                return@launch
            }
            if (pass.length < 8) {
                _uiState.update { AuthUiState.Error("Password must be at least 8 characters long.") }
                return@launch
            }
            if (name.isBlank()) {
                _uiState.update { AuthUiState.Error("Name cannot be empty.") }
                return@launch
            }

            _uiState.update { AuthUiState.Loading }
            val createdUser = authRepository.createUser(name, email, pass)

            if (createdUser != null) {
                // --- THIS IS THE CORRECTED LOGIC ---
                // 1. We call the repository and save the result to a variable named 'token'
                val token = authRepository.requestOtp(email)

                // 2. We now correctly check if the 'token' is not null
                if (token != null) {
                    // 3. We get the userId from the token and store it
                    userIdForVerification = token.userId
                    emailForVerification = email // Also store the email
                    _uiState.update { AuthUiState.NeedsOtpVerification }
                } else {
                    _uiState.update { AuthUiState.Error("Account created, but failed to send OTP.") }
                }
            } else {
                _uiState.update { AuthUiState.Error("Sign up failed. User may already exist.") }
            }
        }
    }
    fun verifyOtpAndLogin(otp: String) {
        viewModelScope.launch {
            val userId = userIdForVerification
            if (userId == null) {
                _uiState.update { AuthUiState.Error("Something went wrong. Please try signing up again.") }
                return@launch
            }

            _uiState.update { AuthUiState.Loading }

            // Use the stored user ID and the user-provided OTP to log in.
            val user = authRepository.verifyOtpAndLogin(userId, otp)
            if (user != null) {
                _uiState.update { AuthUiState.Success }
            } else {
                _uiState.update { AuthUiState.Error("Invalid OTP. Please try again.") }
            }
        }
    }


    fun login(email: String, pass: String) {
        viewModelScope.launch {
            _uiState.update { AuthUiState.Loading }
            val user = authRepository.loginWithPassword(email, pass)
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

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            // After logging out, reset the state to Idle to show the login screen.
            _uiState.update { AuthUiState.Idle }
        }
    }

    fun resetState() {
        _uiState.update { AuthUiState.Idle }
    }
}