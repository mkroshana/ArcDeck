package com.example.data.repository

import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class ArrRepositoryTest {

    /** Build a client whose single interceptor returns [json] with HTTP [code] for any request. */
    private fun clientReturning(json: String, code: Int = 200): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(code)
                    .message(if (code == 200) "OK" else "Error")
                    .body(json.toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()

    @Test
    fun queueParsing_mapsRecordsAndSizeleftJsonKey() = runBlocking {
        val json = """
            {
              "page": 1,
              "pageSize": 20,
              "totalRecords": 2,
              "records": [
                {"id": 5, "title": "Some.Movie.2026", "status": "downloading",
                 "size": 1000.0, "sizeleft": 250.0, "timeleft": "00:10:00",
                 "downloadId": "abc", "protocol": "torrent"},
                {"id": 6, "title": "Another", "status": "queued", "size": 2000.0, "sizeleft": 2000.0}
              ]
            }
        """.trimIndent()

        val repo = ArrRepository(clientReturning(json))
        val result = repo.getActiveQueue("http://10.0.0.1:7878", "key", useDemoFallback = false)

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(2, items.size)
        val first = items[0]
        assertEquals(5, first.id)
        assertEquals("downloading", first.status)
        assertEquals(250.0, first.sizeLeft, 0.0001) // maps the "sizeleft" JSON key
        assertEquals("torrent", first.protocol)
    }

    @Test
    fun historyParsing_readsNestedQualityAndLanguages() = runBlocking {
        val json = """
            {
              "records": [
                {"id": 1, "movieId": 101, "sourceTitle": "X",
                 "quality": {"quality": {"name": "WEBDL-2160p"}},
                 "languages": [{"name": "English"}],
                 "date": "2026-01-01T00:00:00Z", "eventType": "grabbed"}
              ]
            }
        """.trimIndent()

        val repo = ArrRepository(clientReturning(json))
        val result = repo.getHistory("http://10.0.0.1:7878", "key", useDemoFallback = false)

        assertTrue(result.isSuccess)
        val items = result.getOrThrow()
        assertEquals(1, items.size)
        assertEquals("WEBDL-2160p", items[0].quality?.quality?.name)
        assertEquals("English", items[0].languages.first().name)
        assertEquals("grabbed", items[0].eventType)
    }

    @Test
    fun moviesParsing_readsBareJsonArray() = runBlocking {
        val json = """
            [
              {"id": 1, "title": "Anora", "year": 2024, "monitored": true, "hasFile": true,
               "sizeOnDisk": 11300000000, "images": [{"coverType": "poster", "url": "/a.jpg"}]}
            ]
        """.trimIndent()

        val repo = ArrRepository(clientReturning(json))
        val result = repo.getMovies("http://10.0.0.1:7878", "key", useDemoFallback = false)

        assertTrue(result.isSuccess)
        val movies = result.getOrThrow()
        assertEquals(1, movies.size)
        assertEquals("Anora", movies[0].title)
        assertEquals(2024, movies[0].year)
        assertTrue(movies[0].hasFile)
    }

    @Test
    fun demoFallback_returnsDemoDataWithoutNetworkCall() = runBlocking {
        // Interceptor would throw if hit; demo mode must short-circuit before any request.
        val client = OkHttpClient.Builder()
            .addInterceptor { throw IOException("network must not be called in demo mode") }
            .build()

        val repo = ArrRepository(client)
        val result = repo.getActiveQueue("http://10.0.0.1:7878", "key", useDemoFallback = true)

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow().isEmpty())
    }

    @Test
    fun blankBaseUrl_fallsBackToDemoEvenWhenFallbackRequested() = runBlocking {
        val repo = ArrRepository(clientReturning("{}"))
        val result = repo.getMovies("", "key", useDemoFallback = false)

        // Blank URL is treated as "no endpoint configured" -> demo data, not a failure.
        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow().isEmpty())
    }

    @Test
    fun httpError_propagatesFailureWhenFallbackDisabled() = runBlocking {
        val repo = ArrRepository(clientReturning("Server Error", code = 500))
        val result = repo.getActiveQueue("http://10.0.0.1:7878", "key", useDemoFallback = false)

        assertTrue(result.isFailure)
    }
}
