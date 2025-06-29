package dev.bhaswat.aura.network

import com.google.gson.annotations.SerializedName

// This class matches the LearningRequest Pydantic model in our Python backend.
data class LearningRequest(
    @SerializedName("topic")
    val topic: String,
    @SerializedName("hours_per_week")
    val hoursPerWeek: Int,
    @SerializedName("preferred_format")
    val preferredFormat: String,
    @SerializedName("userId")
    val userId: String
)

// The classes below match the LearningPlanResponse Pydantic model.
data class LearningPlanResponse(
    @SerializedName("plan_title")
    val planTitle: String,
    @SerializedName("modules")
    val modules: List<LearningModule>
)

data class LearningModule(
    @SerializedName("week")
    val week: Int,
    @SerializedName("topic")
    val topic: String,
    @SerializedName("resources")
    val resources: List<Resource>
)

data class Resource(
    @SerializedName("title")
    val title: String,
    @SerializedName("url")
    val url: String,
    @SerializedName("type")
    val type: String
)
data class SavePlanRequest(
    @SerializedName("userId")
    val userId: String,
    @SerializedName("planTitle")
    val planTitle: String,
    // We send the modules as a list of simple maps.
    @SerializedName("modules")
    val modules: List<Map<String, Any>>
)

// --- THIS IS THE NEW DATA CLASS FOR THE SAVE RESPONSE ---
data class SavePlanResponse(
    @SerializedName("success")
    val success: Boolean,
    @SerializedName("error")
    val error: String? = null
)