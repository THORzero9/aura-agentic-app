package dev.bhaswat.aura.data

import android.content.Context
import androidx.activity.ComponentActivity
import dev.bhaswat.aura.network.AppwriteClient
import io.appwrite.ID
import io.appwrite.enums.OAuthProvider
import io.appwrite.models.Token
import io.appwrite.models.User
import io.appwrite.services.Account

class AuthRepository(context: Context) {

    private val account = Account(AppwriteClient.getInstance(context))

    /**
     * Creates a new user account with a name, email, and password.
     */
    suspend fun createUser(name: String, email: String, pass: String): User<Map<String, Any>>? {
        return try {
            account.create(
                userId = ID.unique(),
                email = email,
                password = pass,
                name = name
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // UPDATED: This is the correct function to REQUEST the OTP email.
    // It now returns the Token object from Appwrite.
    suspend fun requestOtp(email: String): Token? {
        return try {
            account.createEmailToken(userId = ID.unique(), email = email)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // NEW: This function verifies the OTP and creates a session (logs the user in).
    suspend fun verifyOtpAndLogin(userId: String, otp: String): User<Map<String, Any>>? {
        return try {
            // The 'secret' for this function is the OTP code from the email.
            account.createSession(userId = userId, secret = otp)
            // After creating the session, get the user details to confirm success.
            account.get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Standard login for an existing user with their password.
     */
    suspend fun loginWithPassword(email: String, pass: String): User<Map<String, Any>>? {
        return try {
            account.createEmailPasswordSession(email, pass)
            account.get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Handles the Google Sign-In OAuth flow.
     */
    suspend fun signInWithGoogle(activity: ComponentActivity): User<Map<String, Any>>? {
        return try {
            account.createOAuth2Session(
                activity = activity,
                provider = OAuthProvider.GOOGLE
            )
            account.get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Checks if there is a currently active session.
     */
    suspend fun getCurrentUser(): User<Map<String, Any>>? {
        return try {
            account.get()
        } catch (e: Exception) {
            null
        }
    }
    suspend fun logout() {
        try {
            // Tells Appwrite to delete the session for the currently logged-in user.
            account.deleteSession("current")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}