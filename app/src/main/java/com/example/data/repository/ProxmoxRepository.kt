package com.example.data.repository

import com.example.data.model.ProxmoxResource
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
            val vms = service.getVirtualMachines(token, node).data
            val lxcs = service.getLxcContainers(token, node).data
            
            // Combine them
            Result.success(vms + lxcs)
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
            
            val response = when (action.lowercase()) {
                "start" -> {
                    if (isQemu) service.startVirtualMachine(token, node, resource.vmid)
                    else service.startLxcContainer(token, node, resource.vmid)
                }
                "stop" -> {
                    if (isQemu) service.stopVirtualMachine(token, node, resource.vmid)
                    else service.stopLxcContainer(token, node, resource.vmid)
                }
                "shutdown" -> {
                    if (isQemu) service.shutdownVirtualMachine(token, node, resource.vmid)
                    else service.shutdownLxcContainer(token, node, resource.vmid)
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
}
