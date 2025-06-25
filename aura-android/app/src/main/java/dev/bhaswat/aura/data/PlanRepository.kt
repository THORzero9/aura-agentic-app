package dev.bhaswat.aura.data

import dev.bhaswat.aura.network.ApiClient
import dev.bhaswat.aura.network.LearningPlanResponse
import dev.bhaswat.aura.network.LearningRequest

/**
 * The Repository is the single source of truth for our app's data.
 * It abstracts the data source (in this case, our network API) from the ViewModel.
 */
class PlanRepository {

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
            // If anything goes wrong with the network call (no internet, server error),
            // we catch the exception, print it for debugging, and return null.
            e.printStackTrace()
            null
        }
    }
}