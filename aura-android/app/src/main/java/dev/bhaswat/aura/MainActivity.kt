package dev.bhaswat.aura

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import dev.bhaswat.aura.data.AuthRepository
import dev.bhaswat.aura.ui.AppNavigation
import dev.bhaswat.aura.ui.screens.home.HomeScreen
import dev.bhaswat.aura.ui.theme.AuraTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleVerification(intent)
        enableEdgeToEdge()
        setContent {
            AuraTheme {
                AppNavigation()
            }
        }
    }
    // This is called if the app is already open and receives the link
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleVerification(intent)
    }

    private fun handleVerification(intent: Intent?) {
        val uri = intent?.data
        // Check if the app was opened by our custom verification link
        // This now correctly checks for your custom domain
        if (uri != null && uri.scheme == "https" && uri.host == "bhaswat.social" && uri.path == "/verify") {
            val userId = uri.getQueryParameter("userId")
            val secret = uri.getQueryParameter("secret")

            if (userId != null && secret != null) {
                // We have the keys, now confirm with Appwrite in the background
                lifecycleScope.launch {
                    val authRepository = AuthRepository(applicationContext)
                    val success = authRepository.confirmVerification(userId, secret)

                    // Show a clear message to the user
                    val message = if (success) {
                        "Your email has been successfully verified! Please log in."
                    } else {
                        "Email verification failed. Please try again."
                    }
                    Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}