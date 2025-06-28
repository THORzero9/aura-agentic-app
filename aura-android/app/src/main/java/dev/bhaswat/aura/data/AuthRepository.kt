package dev.bhaswat.aura.data

import android.content.Context
import androidx.activity.ComponentActivity
import dev.bhaswat.aura.network.AppwriteClient
import io.appwrite.enums.OAuthProvider
import io.appwrite.exceptions.AppwriteException
import io.appwrite.models.User
import io.appwrite.services.Account

class AuthRepository(context: Context) {

    private val account = Account(AppwriteClient.getInstance(context))

    suspend fun createUser(email: String, pass: String): User<Map<String , Any>>? {
        return try {
            account.create(
                userId = io.appwrite.ID.unique(),
                email = email,
                password = pass
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun createSession(email: String, pass: String): User<Map<String , Any>>? {
        return try {
            account.createEmailPasswordSession(email, pass)
            account.get()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

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

    suspend fun getCurrentUser(): User<Map<String, Any>>? {
        return try {
            account.get()
        } catch (e: Exception) {
            // If there's no active session, Appwrite throws an exception.
            // We catch it and return null.
            null
        }
    }

    suspend fun requestEmailVerification(): Boolean {
        return try {
            // The URL here must match the data scheme in your AndroidManifest.xml
            // for the CallbackActivity (appwrite-callback-aura-agentic-app)
            val url = "https://bhaswat.social/verify"
            account.createVerification(url = url)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    suspend fun resendEmailVerification(): Boolean {
        return try {
            // This function is often called after a user explicitly requests a resend
            // It relies on the current session.
            val url = "https://bhaswat.social/verify"
            account.createVerification(url = url)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    suspend fun confirmVerification(userId: String, secret: String): Boolean {
        return try {
            // This is the final confirmation step that tells Appwrite the user
            // has successfully clicked the link.
            account.updateVerification(userId = userId, secret = secret)
            true // Return true if the API call succeeds
        } catch (e: Exception) {
            e.printStackTrace()
            false // Return false if an error occurs
        }
    }


    // MODIFIED: Function to create a user, immediately create a session, and verify session status
    suspend fun createUserWithDetails(email: String, pass: String): Pair<User<Map<String, Any>>?, String?> {
        return try {
            val createdUser = account.create(
                userId = io.appwrite.ID.unique(),
                email = email,
                password = pass
            )

            // Attempt to create a session for the newly created user
            account.createEmailPasswordSession(email, pass)

            // NEW: Explicitly get the current user to confirm session is active and has 'account' scope
            val currentUser = account.get()

            Pair(currentUser, null) // Successful creation and session established

        } catch (e: AppwriteException) {
            e.printStackTrace()
            // Return null user and the specific error message from Appwrite
            Pair(null, e.message)
        } catch (e: Exception) {
            e.printStackTrace()
            Pair(null, "An unexpected error occurred during user creation or session login.")
        }
    }
}