package com.example.data.network

import com.example.data.model.ArrQueueResponse
import com.example.data.model.ArrHistoryResponse
import com.example.data.model.ArrMovie
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface ArrService {

    @GET("api/v3/queue")
    suspend fun getDownloadQueue(
        @Header("X-Api-Key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 20
    ): ArrQueueResponse

    @GET("api/v3/history")
    suspend fun getHistory(
        @Header("X-Api-Key") apiKey: String,
        @Query("page") page: Int = 1,
        @Query("pageSize") pageSize: Int = 50,
        @Query("sortKey") sortKey: String = "date",
        @Query("sortDirection") sortDirection: String = "descending"
    ): ArrHistoryResponse

    @GET("api/v3/movie")
    suspend fun getMovies(
        @Header("X-Api-Key") apiKey: String
    ): List<ArrMovie>
}
