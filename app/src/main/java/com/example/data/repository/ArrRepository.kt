package com.example.data.repository

import com.example.data.model.ArrQueueItem
import com.example.data.network.ArrService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class ArrRepository(
    private val okHttpClient: OkHttpClient
) {
    private fun getArrService(baseUrl: String): ArrService {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ArrService::class.java)
    }

    /**
     * Fetch active downloading items.
     */
    suspend fun getActiveQueue(
        baseUrl: String,
        apiKey: String,
        useDemoFallback: Boolean = true
    ): Result<List<ArrQueueItem>> = withContext(Dispatchers.IO) {
        if (useDemoFallback || baseUrl.isBlank() || baseUrl.contains("example.com")) {
            return@withContext Result.success(getDemoQueue())
        }

        try {
            val service = getArrService(baseUrl)
            val response = service.getDownloadQueue(apiKey)
            Result.success(response.records)
        } catch (e: Exception) {
            if (useDemoFallback) {
                Result.success(getDemoQueue())
            } else {
                Result.failure(e)
            }
        }
    }

    private fun getDemoQueue(): List<ArrQueueItem> {
        return listOf(
            ArrQueueItem(1, "Inception.2010.2160p.REMASTERED.HDR.HEVC.Atmos-GEMINI", "downloading", 48_400_000_000.0, 12_100_000_000.0, "00:15:34"),
            ArrQueueItem(2, "The.Sopranos.S01E01.1080p.BluRay.x264.DTS-HDA.2", "downloading", 4300_000_000.0, 3100_000_000.0, "00:04:12"),
            ArrQueueItem(3, "Interstellar.2014.2160p.BluRay.REMUX.HEVC.TrueHD", "paused", 64_000_000_000.0, 52_000_000_000.0, "Paused"),
            ArrQueueItem(4, "Chef\'s.Table.S06E03.1080p.NF.WEBRip.DDP5.1.x264", "downloading", 2100_000_000.0, 210_000_000.0, "00:01:05"),
            ArrQueueItem(5, "Dune.Part.Two.2024.1080p.WEB.H264-AISTUDIO", "queued", 8200_000_000.0, 8200_000_000.0, "In Queue")
        )
    }
}
