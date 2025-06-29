package dev.bhaswat.aura.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dev.bhaswat.aura.network.ApiClient
import dev.bhaswat.aura.network.AppwriteClient
import dev.bhaswat.aura.network.LearningModule
import dev.bhaswat.aura.network.LearningPlanResponse
import dev.bhaswat.aura.network.LearningRequest
import dev.bhaswat.aura.network.SavePlanRequest
import io.appwrite.ID
import io.appwrite.Query
import io.appwrite.services.Databases


/**
 * The Repository is the single source of truth for our app's data.
 * It abstracts the data source (in this case, our network API) from the ViewModel.
 */
class PlanRepository(private val context: Context) {

    private val databases = Databases(AppwriteClient.getInstance(context))
    // We create an instance of AuthRepository so we can find out who is logged in.
    private val authRepository = AuthRepository(context)

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

    suspend fun savePlan(plan: LearningPlanResponse): Boolean {
        return try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) {
                println("Error: No user is logged in. Cannot save plan.")
                return false
            }

            // Create the request object for our new endpoint
            val saveRequest = SavePlanRequest(
                userId = currentUser.id ,
                planTitle = plan.planTitle ,
                // We convert our list of LearningModule objects to a list of simple maps
                modules = plan.modules.map { module ->
                    mapOf(
                        "week" to module.week ,
                        "topic" to module.topic ,
                        "resources" to module.resources.map { res ->
                            mapOf("title" to res.title , "url" to res.url , "type" to res.type)
                        }
                    )
                }
            )

            // Call the new API endpoint
            val response = ApiClient.apiService.savePlan(saveRequest)

            // Return true if the backend confirms the save was successful
            response.success
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    suspend fun getSavedPlans(): List<LearningPlanResponse> {
        return try {
            val currentUser = authRepository.getCurrentUser()
            if (currentUser == null) return emptyList()

            val response = databases.listDocuments(
                databaseId = databaseId,
                collectionId = collectionId,
                queries = listOf(Query.equal("userId", currentUser.id))
            )

            response.documents.map { document ->
                val modulesJson = document.data["modules"] as String
                val moduleListType = object : TypeToken<List<LearningModule>>() {}.type
                val modules: List<LearningModule> = Gson().fromJson(modulesJson, moduleListType)

                LearningPlanResponse(
                    planTitle = document.data["planTitle"] as String,
                    modules = modules
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}