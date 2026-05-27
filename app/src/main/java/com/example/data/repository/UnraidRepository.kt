package com.example.data.repository

import com.example.data.model.UnraidPoolInfo
import com.example.data.network.UnraidGraphQLClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class UnraidRepository(
    private val graphQLClient: UnraidGraphQLClient
) {
    /**
     * Fetch Unraid array storage pools.
     */
    suspend fun getStoragePools(
        endpointUrl: String,
        authToken: String?,
        useDemoFallback: Boolean = true
    ): Result<List<UnraidPoolInfo>> = withContext(Dispatchers.IO) {
        if (useDemoFallback || endpointUrl.isBlank() || endpointUrl.contains("example.com")) {
            return@withContext Result.success(getDemoPools())
        }

        val graphQLQuery = """
            query {
                storage {
                    pools {
                        name
                        status
                        usagePercent
                        sizeBytes
                        freeBytes
                    }
                }
            }
        """.trimIndent()

        try {
            val response = graphQLClient.executeQuery(
                endpointUrl = endpointUrl,
                authToken = authToken,
                query = graphQLQuery
            )
            
            val poolsList = response.data?.storage?.pools
            if (poolsList != null) {
                Result.success(poolsList)
            } else {
                Result.failure(IOException("No storage pool data returned from Unraid GraphQL"))
            }
        } catch (e: Exception) {
            if (useDemoFallback) {
                Result.success(getDemoPools())
            } else {
                Result.failure(e)
            }
        }
    }

    private fun getDemoPools(): List<UnraidPoolInfo> {
        return listOf(
            UnraidPoolInfo("Main Array (Parity-Secured)", "ONLINE", 64.2f, 40000000000000L, 14320000000000L),
            UnraidPoolInfo("Cache Disk Pool (SSD)", "ONLINE", 28.5f, 2000000000000L, 1430000000000L),
            UnraidPoolInfo("Appdata Fast TierNVMe", "ONLINE", 48.7f, 1000000000000L, 513000000000L)
        )
    }
}
