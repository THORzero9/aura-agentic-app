package dev.bhaswat.aura.data

import android.content.Context
import androidx.activity.ComponentActivity
import dev.bhaswat.aura.network.AppwriteClient
import io.appwrite.enums.OAuthProvider
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
}