package com.example.data.repository

import com.example.data.network.UnraidGraphQLClient
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException

class UnraidRepositoryTest {

    private val endpoint = "http://10.0.0.2/graphql"

    /** Build a repository whose GraphQL client returns [json] with HTTP [code] for any request. */
    private fun repoReturning(json: String, code: Int = 200): UnraidRepository {
        val client = OkHttpClient.Builder()
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
        return UnraidRepository(UnraidGraphQLClient(client, Moshi.Builder().build()))
    }

    @Test
    fun arrayOverview_parsesStateAndCapacity() = runBlocking {
        val json = """
            {"data": {"array": {
                "state": "STARTED",
                "capacity": {"kilobytes": {"free": "10", "used": "90", "total": "100"}}
            }}}
        """.trimIndent()

        val result = repoReturning(json).getArrayOverview(endpoint, "token", useDemoFallback = false)

        assertTrue(result.isSuccess)
        val array = result.getOrThrow()
        assertEquals("STARTED", array.state)
        assertEquals("90", array.capacity?.kilobytes?.used)
        assertEquals("100", array.capacity?.kilobytes?.total)
    }

    @Test
    fun vms_mapsDomainFieldToDomainsList() = runBlocking {
        val json = """
            {"data": {"vms": {"domain": [
                {"id": "vm:1", "name": "Windows 11", "state": "running"},
                {"id": "vm:2", "name": "Ubuntu", "state": "shutoff"}
            ]}}}
        """.trimIndent()

        val result = repoReturning(json).getVMs(endpoint, "token", useDemoFallback = false)

        assertTrue(result.isSuccess)
        val vms = result.getOrThrow()
        assertEquals(2, vms.size)
        assertEquals("Windows 11", vms[0].name)
        assertEquals("running", vms[0].state)
    }

    @Test
    fun dockerContainers_parseNamesList() = runBlocking {
        val json = """
            {"data": {"docker": {"containers": [
                {"id": "d:1", "names": ["plex"], "image": "plexinc/pms-docker",
                 "state": "running", "status": "Up 7 days", "autoStart": true}
            ]}}}
        """.trimIndent()

        val result = repoReturning(json).getDockerContainers(endpoint, "token", useDemoFallback = false)

        assertTrue(result.isSuccess)
        val containers = result.getOrThrow()
        assertEquals(1, containers.size)
        assertEquals("plex", containers[0].names.first())
        assertEquals("running", containers[0].state)
    }

    @Test
    fun demoFallback_returnsDemoArrayWithoutNetworkCall() = runBlocking {
        val throwingClient = OkHttpClient.Builder()
            .addInterceptor { throw IOException("network must not be called in demo mode") }
            .build()
        val repo = UnraidRepository(UnraidGraphQLClient(throwingClient, Moshi.Builder().build()))

        val result = repo.getArrayOverview(endpoint, "token", useDemoFallback = true)

        assertTrue(result.isSuccess)
        assertNotNull(result.getOrThrow().capacity)
    }

    @Test
    fun httpError_propagatesFailureWhenFallbackDisabled() = runBlocking {
        val result = repoReturning("Server Error", code = 500)
            .getArrayOverview(endpoint, "token", useDemoFallback = false)

        assertTrue(result.isFailure)
    }

    @Test
    fun missingArrayField_isFailureWhenFallbackDisabled() = runBlocking {
        // Valid GraphQL envelope but no array payload.
        val result = repoReturning("""{"data": {}}""")
            .getArrayOverview(endpoint, "token", useDemoFallback = false)

        assertTrue(result.isFailure)
    }

    @Test
    fun httpError_fallsBackToDemoWhenEnabled() = runBlocking {
        val result = repoReturning("Server Error", code = 500)
            .getArrayOverview(endpoint, "token", useDemoFallback = true)

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow().disks.isEmpty())
    }
}
