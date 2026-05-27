package com.example.data.network

import com.example.data.model.ArrQueueResponse
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
}
