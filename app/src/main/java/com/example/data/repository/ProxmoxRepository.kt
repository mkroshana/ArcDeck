package com.example.data.repository

import com.example.data.model.ProxmoxResource
import com.example.data.model.ProxmoxNodeStatus
import com.example.data.network.ProxmoxService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class ProxmoxRepository(
    private val okHttpClient: OkHttpClient
) {
    // Generate a Moshi Retrofit service dynamically based on configurable URL
    private fun getProxmoxService(baseUrl: String): ProxmoxService {
        val sanitizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(ProxmoxService::class.java)
    }

    /**
     * Fetch VMs and Containers from Proxmox VE.
     */
    suspend fun getResources(
        baseUrl: String,
        token: String,
        node: String,
        useDemoFallback: Boolean = true
    ): Result<List<ProxmoxResource>> = withContext(Dispatchers.IO) {
        if (useDemoFallback || baseUrl.isBlank() || baseUrl.contains("example.com")) {
            return@withContext Result.success(getDemoResources())
        }

        try {
            val service = getProxmoxService(baseUrl)
            val authHeader = sanitizeToken(token)
            val vms = service.getVirtualMachines(authHeader, node).data.map { it.copy(type = "qemu") }
            val lxcs = service.getLxcContainers(authHeader, node).data.map { it.copy(type = "lxc") }
            
            // Combine and sort them by VMID
            Result.success((vms + lxcs).sortedBy { it.vmid })
        } catch (e: Exception) {
            // Handle error gracefully, or return demo fallback if requested
            if (useDemoFallback) {
                Result.success(getDemoResources())
            } else {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetch Node Status (CPU/RAM telemetry) from Proxmox VE.
     */
    suspend fun getNodeStatus(
        baseUrl: String,
        token: String,
        node: String,
        useDemoFallback: Boolean = true
    ): Result<ProxmoxNodeStatus> = withContext(Dispatchers.IO) {
        if (useDemoFallback || baseUrl.isBlank() || baseUrl.contains("example.com")) {
            return@withContext Result.success(getDemoNodeStatus())
        }

        try {
            val service = getProxmoxService(baseUrl)
            val authHeader = sanitizeToken(token)
            val response = service.getNodeStatus(authHeader, node)
            Result.success(response.data)
        } catch (e: Exception) {
            if (useDemoFallback) {
                Result.success(getDemoNodeStatus())
            } else {
                Result.failure(e)
            }
        }
    }

    private fun getDemoNodeStatus(): ProxmoxNodeStatus {
        val randomCpu = kotlin.random.Random.nextDouble(0.15, 0.75)
        val randomMem = kotlin.random.Random.nextLong(4_000_000_000L, 12_000_000_000L)
        return ProxmoxNodeStatus(
            cpu = randomCpu,
            maxCpu = 8,
            memoryRaw = mapOf(
                "total" to 16_777_216_000L,
                "used" to randomMem,
                "free" to 16_777_216_000L - randomMem
            ),
            uptime = 120450L,
            status = "online"
        )
    }

    /**
     * Handle VM/LXC Power state lifecycle transitions.
     * action: "start", "stop", "shutdown"
     */
    suspend fun performPowerAction(
        baseUrl: String,
        token: String,
        node: String,
        resource: ProxmoxResource,
        action: String,
        useDemoFallback: Boolean = true
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        if (useDemoFallback || baseUrl.isBlank() || baseUrl.contains("example.com")) {
            return@withContext Result.success(true)
        }

        try {
            val service = getProxmoxService(baseUrl)
            val isQemu = resource.type == "qemu" || resource.type == "VM"
            val authHeader = sanitizeToken(token)
            
            val response = when (action.lowercase()) {
                "start" -> {
                    if (isQemu) service.startVirtualMachine(authHeader, node, resource.vmid)
                    else service.startLxcContainer(authHeader, node, resource.vmid)
                }
                "stop" -> {
                    if (isQemu) service.stopVirtualMachine(authHeader, node, resource.vmid)
                    else service.stopLxcContainer(authHeader, node, resource.vmid)
                }
                "shutdown" -> {
                    if (isQemu) service.shutdownVirtualMachine(authHeader, node, resource.vmid)
                    else service.shutdownLxcContainer(authHeader, node, resource.vmid)
                }
                else -> throw IllegalArgumentException("Unknown command: $action")
            }
            Result.success(response.taskUpid != null)
        } catch (e: Exception) {
            if (useDemoFallback) {
                Result.success(true)
            } else {
                Result.failure(e)
            }
        }
    }

    private fun getDemoResources(): List<ProxmoxResource> {
        return listOf(
            ProxmoxResource(100, "plex-media-server", "qemu", "running", 14.5, 4, 3420000000L, 8589000000L, 120450L),
            ProxmoxResource(101, "pihole-dns-primary", "lxc", "running", 2.1, 1, 142000000L, 536000000L, 854900L),
            ProxmoxResource(102, "home-assistant-core", "qemu", "running", 8.4, 2, 1024000000, 4294000000, 2450000),
            ProxmoxResource(103, "nextcloud-personal", "lxc", "stopped", 0.0, 2, 0, 4294000000, 0),
            ProxmoxResource(104, "unifi-controller", "lxc", "running", 5.2, 2, 850000000, 2147000000, 154300L),
            ProxmoxResource(105, "minecraft-pvp", "qemu", "stopped", 0.0, 4, 0, 8589000000, 0)
        )
    }

    private fun sanitizeToken(token: String): String {
        val trimmed = token.trim()
        var tokenValue = trimmed
        val prefixes = listOf("pveapitoken=", "pveapitoken:", "pveapitoken ")
        for (prefix in prefixes) {
            if (tokenValue.lowercase().startsWith(prefix)) {
                tokenValue = tokenValue.substring(prefix.length).trim()
                break
            }
        }
        val regex = Regex("""^([a-zA-Z0-9.\-_]+@[a-zA-Z0-9.\-_]+)!([a-zA-Z0-9.\-_]+)[=: ]\s*(.+)$""")
        val matchResult = regex.find(tokenValue)
        if (matchResult != null) {
            val userId = matchResult.groupValues[1]
            val tokenId = matchResult.groupValues[2]
            val secret = matchResult.groupValues[3].trim()
            return "PVEAPIToken=$userId!$tokenId=$secret"
        }
        val firstExclam = tokenValue.indexOf('!')
        if (firstExclam != -1) {
            val userPart = tokenValue.substring(0, firstExclam).trim()
            val rest = tokenValue.substring(firstExclam + 1).trim()
            val eqIndex = rest.indexOf('=')
            val colonIndex = rest.indexOf(':')
            val sepIndex = when {
                eqIndex != -1 && colonIndex != -1 -> minOf(eqIndex, colonIndex)
                eqIndex != -1 -> eqIndex
                colonIndex != -1 -> colonIndex
                else -> -1
            }
            if (sepIndex != -1) {
                val tokenId = rest.substring(0, sepIndex).trim()
                val secret = rest.substring(sepIndex + 1).trim()
                return "PVEAPIToken=$userPart!$tokenId=$secret"
            }
        }
        return if (tokenValue.startsWith("PVEAPIToken=")) tokenValue else "PVEAPIToken=$tokenValue"
    }
}
