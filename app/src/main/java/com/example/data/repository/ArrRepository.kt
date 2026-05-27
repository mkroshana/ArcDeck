package com.example.data.repository

import com.example.data.model.*
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

    /**
     * Fetch active history logs.
     */
    suspend fun getHistory(
        baseUrl: String,
        apiKey: String,
        useDemoFallback: Boolean = true
    ): Result<List<ArrHistoryItem>> = withContext(Dispatchers.IO) {
        if (useDemoFallback || baseUrl.isBlank() || baseUrl.contains("example.com")) {
            return@withContext Result.success(getDemoHistory())
        }

        try {
            val service = getArrService(baseUrl)
            val response = service.getHistory(apiKey)
            Result.success(response.records)
        } catch (e: Exception) {
            if (useDemoFallback) {
                Result.success(getDemoHistory())
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetch current movie library.
     */
    suspend fun getMovies(
        baseUrl: String,
        apiKey: String,
        useDemoFallback: Boolean = true
    ): Result<List<ArrMovie>> = withContext(Dispatchers.IO) {
        if (useDemoFallback || baseUrl.isBlank() || baseUrl.contains("example.com")) {
            return@withContext Result.success(getDemoMovies())
        }

        try {
            val service = getArrService(baseUrl)
            val response = service.getMovies(apiKey)
            Result.success(response)
        } catch (e: Exception) {
            if (useDemoFallback) {
                Result.success(getDemoMovies())
            } else {
                Result.failure(e)
            }
        }
    }

    private fun getDemoQueue(): List<ArrQueueItem> {
        return listOf(
            ArrQueueItem(
                id = 1,
                title = "Over Your Dead Body (2026) [2160p] [WEBRip] [x265] [10bit] [5.1] [ UIndex.org ]",
                status = "downloading",
                size = 5_040_000_000.0,
                sizeLeft = 4_940_000_000.0,
                timeLeft = "00:39:29",
                downloadId = "torrent_1",
                protocol = "torrent"
            )
        )
    }

    private fun getDemoHistory(): List<ArrHistoryItem> {
        val q2160 = ArrHistoryQuality(ArrHistoryQualityDetails("WEBRip-2160p"))
        val qWebdl = ArrHistoryQuality(ArrHistoryQualityDetails("WEBDL-2160p"))
        val langEnglish = listOf(ArrHistoryLanguage("English"))
        val langMulti = listOf(ArrHistoryLanguage("Multi-Language"))

        return listOf(
            ArrHistoryItem(1, 101, "Over Your Dead Body", q2160, langEnglish, "2026-05-28T01:50:00Z", "grabbed"),
            ArrHistoryItem(2, 102, "The Super Mario Galaxy Movie", qWebdl, langMulti, "2026-05-20T22:15:00Z", "downloadFolderImported"),
            ArrHistoryItem(3, 102, "The Super Mario Galaxy Movie", qWebdl, langEnglish, "2026-05-20T21:45:00Z", "grabbed"),
            ArrHistoryItem(4, 103, "The Babysitter", q2160, langEnglish, "2026-04-28T18:10:00Z", "downloadFolderImported"),
            ArrHistoryItem(5, 104, "Kingsman: The Golden Circle", qWebdl, langMulti, "2026-04-20T12:05:00Z", "downloadFolderImported"),
            ArrHistoryItem(6, 105, "Kingsman: The Secret Service", qWebdl, langMulti, "2026-04-16T15:40:00Z", "downloadFolderImported"),
            ArrHistoryItem(7, 106, "Thrash", qWebdl, langEnglish, "2026-04-14T09:20:00Z", "downloadFolderImported"),
            ArrHistoryItem(8, 107, "Heretic", qWebdl, langEnglish, "2026-04-11T14:15:00Z", "downloadFolderImported"),
            ArrHistoryItem(9, 108, "The Housemaid", qWebdl, langEnglish, "2026-04-10T17:35:00Z", "downloadFolderImported"),
            ArrHistoryItem(10, 109, "Anora", qWebdl, langEnglish, "2026-04-10T11:00:00Z", "downloadFolderImported")
        )
    }

    private fun getDemoMovies(): List<ArrMovie> {
        return listOf(
            ArrMovie(1, "Over Your Dead Body", 2026, true, true, 5_040_000_000L, listOf(ArrMovieImage("poster", "", "https://image.tmdb.org/t/p/w500/wPbg50K365E2Hox9P76450125K1.jpg")), "2026-05-28T01:50:00Z"),
            ArrMovie(2, "Kingsman: The Golden Circle", 2017, true, true, 12_400_000_000L, listOf(ArrMovieImage("poster", "", "https://image.tmdb.org/t/p/w500/qis54rAOS44v3n6c434F4F6D560.jpg")), "2026-04-16T15:40:00Z"),
            ArrMovie(3, "Kingsman: The Secret Service", 2014, true, true, 10_800_000_000L, listOf(ArrMovieImage("poster", "", "https://image.tmdb.org/t/p/w500/o5T8wzKi6Y57WvPqU95qj4K1vS.jpg")), "2026-04-16T15:40:00Z"),
            ArrMovie(4, "Thrash", 2026, true, true, 8_200_000_000L, listOf(ArrMovieImage("poster", "", "https://image.tmdb.org/t/p/w500/b8O4Y69mP3aM147Y6X8dYp3S5R0.jpg")), "2026-04-14T09:20:00Z"),
            ArrMovie(5, "The Babysitter", 2017, true, true, 6_500_000_000L, listOf(ArrMovieImage("poster", "", "https://image.tmdb.org/t/p/w500/2L2f3sV4M6X8U9aB7Y6y34C8m2v.jpg")), "2026-04-28T18:10:00Z"),
            ArrMovie(6, "Heretic", 2026, true, true, 9_100_000_000L, listOf(ArrMovieImage("poster", "", "https://image.tmdb.org/t/p/w500/a8B9Y10mO3bN158Z6Y9dYq3T5R1.jpg")), "2026-04-11T14:15:00Z"),
            ArrMovie(7, "The Housemaid", 2026, true, true, 7_800_000_000L, listOf(ArrMovieImage("poster", "", "https://image.tmdb.org/t/p/w500/c9C10nP4cO1O168A6Y8dYp3U5R2.jpg")), "2026-04-10T17:35:00Z"),
            ArrMovie(8, "Anora", 2024, true, true, 11_300_000_000L, listOf(ArrMovieImage("poster", "", "https://image.tmdb.org/t/p/w500/wPbg50K365E2Hox9P76450125K1.jpg")), "2026-04-10T11:00:00Z")
        )
    }
}
