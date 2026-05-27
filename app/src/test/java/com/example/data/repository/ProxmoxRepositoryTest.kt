package com.example.data.repository

import com.example.data.model.ProxmoxResource
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ProxmoxRepositoryTest {

    @Test
    fun testTokenFormatting_withPrefix() = runBlocking {
        var interceptedHeader: String? = null
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                interceptedHeader = request.header("Authorization")
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{\"data\":[]}".toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()

        val repository = ProxmoxRepository(client)
        val result = repository.getResources(
            baseUrl = "https://192.168.1.10:8006",
            token = "PVEAPIToken=root@pam!token=1234",
            node = "pve",
            useDemoFallback = false
        )

        assertTrue(result.isSuccess)
        assertEquals("PVEAPIToken=root@pam!token=1234", interceptedHeader)
    }

    @Test
    fun testTokenFormatting_withoutPrefix() = runBlocking {
        var interceptedHeader: String? = null
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                interceptedHeader = request.header("Authorization")
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{\"data\":[]}".toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()

        val repository = ProxmoxRepository(client)
        val result = repository.getResources(
            baseUrl = "https://192.168.1.10:8006",
            token = "root@pam!token=1234",
            node = "pve",
            useDemoFallback = false
        )

        assertTrue(result.isSuccess)
        assertEquals("PVEAPIToken=root@pam!token=1234", interceptedHeader)
    }

    @Test
    fun testTokenFormatting_withWhitespace() = runBlocking {
        var interceptedHeader: String? = null
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                interceptedHeader = request.header("Authorization")
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{\"data\":[]}".toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()

        val repository = ProxmoxRepository(client)
        val result = repository.getResources(
            baseUrl = "https://192.168.1.10:8006",
            token = "  root@pam!token=1234  ",
            node = "pve",
            useDemoFallback = false
        )

        assertTrue(result.isSuccess)
        assertEquals("PVEAPIToken=root@pam!token=1234", interceptedHeader)
    }

    @Test
    fun testTokenFormatting_withColonSeparator() = runBlocking {
        var interceptedHeader: String? = null
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                interceptedHeader = request.header("Authorization")
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{\"data\":[]}".toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()

        val repository = ProxmoxRepository(client)
        val result = repository.getResources(
            baseUrl = "https://192.168.1.10:8006",
            token = "root@pam!token:1234",
            node = "pve",
            useDemoFallback = false
        )

        assertTrue(result.isSuccess)
        assertEquals("PVEAPIToken=root@pam!token=1234", interceptedHeader)
    }

    @Test
    fun testTokenFormatting_withColonPrefixAndColonSeparator() = runBlocking {
        var interceptedHeader: String? = null
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                interceptedHeader = request.header("Authorization")
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body("{\"data\":[]}".toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()

        val repository = ProxmoxRepository(client)
        val result = repository.getResources(
            baseUrl = "https://192.168.1.10:8006",
            token = "PVEAPIToken: root@pam!token:1234",
            node = "pve",
            useDemoFallback = false
        )

        assertTrue(result.isSuccess)
        assertEquals("PVEAPIToken=root@pam!token=1234", interceptedHeader)
    }

    @Test
    fun testResourcesParsing_whenTypeIsMissingFromApi() = runBlocking {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                val path = request.url.encodedPath
                val responseJson = if (path.contains("qemu")) {
                    """{"data":[{"vmid":150,"name":"haos","status":"running"}]}"""
                } else {
                    """{"data":[{"vmid":100,"name":"traefik","status":"running"}]}"""
                }
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(responseJson.toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()

        val repository = ProxmoxRepository(client)
        val result = repository.getResources(
            baseUrl = "https://192.168.1.10:8006",
            token = "root@pam!token=1234",
            node = "pve",
            useDemoFallback = false
        )

        assertTrue(result.isSuccess)
        val resources = result.getOrThrow()
        assertEquals(2, resources.size) // 1 from VM (qemu) call, 1 from LXC (lxc) call
        
        // Assert sorting: vmid 100 should come first (LXC), then vmid 150 (VM)
        val first = resources[0]
        assertEquals(100, first.vmid)
        assertEquals("traefik", first.name)
        assertEquals("lxc", first.type) // Mapped and sorted first
        
        val second = resources[1]
        assertEquals(150, second.vmid)
        assertEquals("haos", second.name)
        assertEquals("qemu", second.type) // Mapped and sorted second
    }

    @Test
    fun testNodeStatusParsing() = runBlocking {
        val jsonResponse = """
            {
              "data": {
                "cpu": 0.25,
                "maxcpu": 4,
                "mem": {
                  "total": 8589934592,
                  "used": 4294967296,
                  "free": 4294967296
                },
                "uptime": 12345,
                "status": "online"
              }
            }
        """.trimIndent()

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(jsonResponse.toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()

        val repository = ProxmoxRepository(client)
        val result = repository.getNodeStatus(
            baseUrl = "https://192.168.1.10:8006",
            token = "root@pam!token=1234",
            node = "pve",
            useDemoFallback = false
        )

        assertTrue(result.isSuccess)
        val status = result.getOrThrow()
        assertEquals(0.25, status.cpu, 0.0001)
        assertEquals(4, status.maxCpu)
        assertEquals(4294967296L, status.mem)
        assertEquals(8589934592L, status.maxmem)
        assertEquals(12345L, status.uptime)
        assertEquals("online", status.status)
    }

    @Test
    fun testStorageParsing() = runBlocking {
        val jsonResponse = """
            {
              "data": [
                {
                  "storage": "local",
                  "type": "dir",
                  "active": 1,
                  "total": 1000000000,
                  "used": 400000000,
                  "avail": 600000000,
                  "shared": 0
                },
                {
                  "storage": "backup",
                  "type": "nfs",
                  "active": 0,
                  "total": 0,
                  "used": 0,
                  "avail": 0,
                  "shared": 1
                }
              ]
            }
        """.trimIndent()

        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Response.Builder()
                    .request(request)
                    .protocol(Protocol.HTTP_1_1)
                    .code(200)
                    .message("OK")
                    .body(jsonResponse.toResponseBody("application/json".toMediaTypeOrNull()))
                    .build()
            }
            .build()

        val repository = ProxmoxRepository(client)
        val result = repository.getNodeStorage(
            baseUrl = "https://192.168.1.10:8006",
            token = "root@pam!token=1234",
            node = "pve",
            useDemoFallback = false
        )

        assertTrue(result.isSuccess)
        val storageList = result.getOrThrow()
        assertEquals(2, storageList.size)

        val first = storageList[0]
        assertEquals("local", first.storage)
        assertEquals("dir", first.type)
        assertEquals(1, first.active)
        assertEquals(1000000000L, first.total)
        assertEquals(400000000L, first.used)
        assertEquals(600000000L, first.avail)
        assertEquals(0, first.shared)

        val second = storageList[1]
        assertEquals("backup", second.storage)
        assertEquals("nfs", second.type)
        assertEquals(0, second.active)
        assertEquals(0L, second.total)
        assertEquals(0L, second.used)
        assertEquals(0L, second.avail)
        assertEquals(1, second.shared)
    }
}

