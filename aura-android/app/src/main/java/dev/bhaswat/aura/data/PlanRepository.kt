package dev.bhaswat.aura.data

import android.content.Context
import com.google.gson.Gson
import dev.bhaswat.aura.network.ApiClient
import dev.bhaswat.aura.network.AppwriteClient
import dev.bhaswat.aura.network.LearningPlanResponse
import dev.bhaswat.aura.network.LearningRequest
import io.appwrite.ID
import io.appwrite.services.Databases

/**
 * The Repository is the single source of truth for our app's data.
 * It abstracts the data source (in this case, our network API) from the ViewModel.
 */
class PlanRepository(private val context: Context) {

    private val databaseId = "685da5cc0028a4d81d0f"
    private val collectionId = "685da5d700336bdeab10"


    /**
     * This function calls our backend agent to generate a learning plan.
     * It's a suspend function so it can be called safely from a coroutine.
     *
     * @param request The LearningRequest object containing the user's input.
     * @return The LearningPlanResponse from the server, or null if an error occurs.
     */
    suspend fun generateLearningPlan(request: LearningRequest): LearningPlanResponse? {
        return try {
            // Call the generatePlan function from our ApiService.
            // Retrofit handles all the complex networking and JSON parsing for us.
            ApiClient.apiService.generatePlan(request)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // --- THIS FUNCTION IS NOW CORRECTLY PLACED ---
    suspend fun savePlan(plan: LearningPlanResponse): Boolean {
        return try {
            val appwriteClient = AppwriteClient.getInstance(context)
            val databases = Databases(appwriteClient)

            val planModulesJson = Gson().toJson(plan.modules)

            val data = mapOf(
                "planTitle" to plan.planTitle,
                "modules" to planModulesJson,
                "userId" to "anonymous_user"
            )

            databases.createDocument(
                databaseId = databaseId,
                collectionId = collectionId,
                documentId = ID.unique(),
                data = data
            )
            println("Successfully saved plan to Appwrite from Android.")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}