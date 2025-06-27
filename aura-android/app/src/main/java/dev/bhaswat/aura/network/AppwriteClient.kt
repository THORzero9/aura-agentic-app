package dev.bhaswat.aura.network

import android.content.Context
import io.appwrite.Client

object AppwriteClient {
    private lateinit var client: Client

    fun getInstance(context: Context): Client {
        if (!::client.isInitialized) {
            client = Client(context)
                .setEndpoint("https://cloud.appwrite.io/v1")
                .setProject("aura-agentic-app")
        }
        return client
    }
}