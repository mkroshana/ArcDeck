package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ============================================================================
// PROXMOX MODELS
// ============================================================================

@JsonClass(generateAdapter = true)
data class ProxmoxResourceResponse(
    @Json(name = "data") val data: List<ProxmoxResource>
)

@JsonClass(generateAdapter = true)
data class ProxmoxResource(
    @Json(name = "vmid") val vmid: Int,
    @Json(name = "name") val name: String,
    @Json(name = "type") val type: String, // "qemu" (VM) or "lxc" (Container)
    @Json(name = "status") val status: String, // "running", "stopped"
    @Json(name = "cpu") val cpuUsage: Double = 0.0,
    @Json(name = "maxcpu") val maxCpu: Int = 1,
    @Json(name = "mem") val memoryUsed: Long = 0L,
    @Json(name = "maxmem") val maxMemory: Long = 1024L,
    @Json(name = "uptime") val uptime: Long = 0L
)

@JsonClass(generateAdapter = true)
data class ProxmoxPowerActionResponse(
    @Json(name = "data") val taskUpid: String?
)

// ============================================================================
// UNRAID MODELS
// ============================================================================

@JsonClass(generateAdapter = true)
data class UnraidGraphQLRequest(
    val query: String,
    val variables: Map<String, Any> = emptyMap()
)

@JsonClass(generateAdapter = true)
data class UnraidGraphQLResponse(
    @Json(name = "data") val data: UnraidGraphQLData?
)

@JsonClass(generateAdapter = true)
data class UnraidGraphQLData(
    @Json(name = "storage") val storage: UnraidStorageInfo?
)

@JsonClass(generateAdapter = true)
data class UnraidStorageInfo(
    @Json(name = "pools") val pools: List<UnraidPoolInfo>?
)

@JsonClass(generateAdapter = true)
data class UnraidPoolInfo(
    @Json(name = "name") val name: String,
    @Json(name = "status") val status: String, // "ONLINE", "DEGRADED", "OFFLINE"
    @Json(name = "usagePercent") val usagePercent: Float,
    @Json(name = "sizeBytes") val sizeBytes: Long,
    @Json(name = "freeBytes") val freeBytes: Long
)

// ============================================================================
// *ARR (SONARR/RADARR) MODELS
// ============================================================================

@JsonClass(generateAdapter = true)
data class ArrQueueResponse(
    @Json(name = "page") val page: Int = 1,
    @Json(name = "pageSize") val pageSize: Int = 10,
    @Json(name = "totalRecords") val totalRecords: Int = 0,
    @Json(name = "records") val records: List<ArrQueueItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ArrQueueItem(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "status") val status: String, // "downloading", "completed", "paused", "queued"
    @Json(name = "size") val size: Double = 0.0, // Total size in bytes
    @Json(name = "sizeleft") val sizeLeft: Double = 0.0, // Remaining bytes
    @Json(name = "timeleft") val timeLeft: String? = null,
    @Json(name = "downloadId") val downloadId: String? = null,
    @Json(name = "protocol") val protocol: String? = "usenet" // "torrent" or "usenet"
)
