package dev.bhaswat.aura.network

import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
   /* @POST("api/generate-plan")
    suspend fun generatePlan(@Body request: LearningRequest): LearningPlanResponse
    @POST("api/save-plan")
    suspend fun savePlan(@Body request: SavePlanRequest): SavePlanResponse*/
   @POST(".")
   suspend fun generatePlan(@Body request: LearningRequest): LearningPlanResponse
    @POST(".")
    suspend fun savePlan(@Body request: SavePlanRequest): SavePlanResponse
}