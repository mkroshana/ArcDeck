package com.example.data.network

import com.example.data.model.UnraidGraphQLRequest
import com.example.data.model.UnraidGraphQLResponse
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class UnraidGraphQLClient(
    private val httpClient: OkHttpClient,
    private val moshi: Moshi
) {
    private val mediaType = "application/json; charset=utf-8".toMediaType()
    private val requestAdapter = moshi.adapter(UnraidGraphQLRequest::class.java)
    private val responseAdapter = moshi.adapter(UnraidGraphQLResponse::class.java)

    /**
     * Executes a GraphQL query against the specified Unraid endpoint.
     */
    suspend fun executeQuery(
        endpointUrl: String,
        authToken: String?, // Auth token placeholder header if required
        query: String,
        variables: Map<String, Any> = emptyMap()
    ): UnraidGraphQLResponse {
        val payload = UnraidGraphQLRequest(query, variables)
        val jsonPayload = requestAdapter.toJson(payload)
        
        val requestBuilder = Request.Builder()
            .url(endpointUrl)
            .post(jsonPayload.toRequestBody(mediaType))
            
        if (!authToken.isNullOrEmpty()) {
            // Support both Bearer token and API key auth
            if (authToken.startsWith("Bearer ", ignoreCase = true)) {
                requestBuilder.header("Authorization", authToken)
            } else {
                requestBuilder.header("Authorization", "Bearer $authToken")
                requestBuilder.header("x-api-key", authToken)
            }
        }

        val request = requestBuilder.build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected HTTP response code: ${response.code}")
            }
            val bodyString = response.body?.string() ?: throw IOException("Empty response body")
            return responseAdapter.fromJson(bodyString) ?: throw IOException("Failed to parse GraphQL response")
        }
    }
}
