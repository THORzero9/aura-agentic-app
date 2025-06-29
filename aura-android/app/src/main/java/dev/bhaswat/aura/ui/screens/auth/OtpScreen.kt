package dev.bhaswat.aura.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.bhaswat.aura.ui.theme.AuraTheme
import dev.bhaswat.aura.ui.theme.PrimaryText

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    authViewModel: AuthViewModel,
    onVerificationSuccess: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsStateWithLifecycle()

    var otpValue by remember { mutableStateOf("") }

    // This effect listens for the Success state and navigates when it occurs
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) {
            onVerificationSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Check Your Email",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )
            Text(
                text = "We've sent a 6-digit code to your email address. Please enter it below to continue.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            OutlinedTextField(
                value = otpValue,
                onValueChange = { if (it.length <= 6) otpValue = it },
                label = { Text("6-Digit Code") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword) ,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Show error message if it exists
            if (uiState is AuthUiState.Error) {
                Text(
                    text = (uiState as AuthUiState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { authViewModel.verifyOtpAndLogin(otpValue) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                // The button is only enabled when the user has entered 6 digits
                enabled = otpValue.length == 6
            ) {
                Text("Verify & Login")
            }
        }

        if (uiState is AuthUiState.Loading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OtpScreenPreview() {
    AuraTheme {
        // OtpScreen(onVerificationSuccess = {})
    }
}