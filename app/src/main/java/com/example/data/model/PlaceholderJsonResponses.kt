package com.example.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Proxmox VE API Response Placeholders
 * Commonly fetched from: GET /api2/json/nodes
 */
@JsonClass(generateAdapter = true)
data class ProxmoxClusterResponse(
    @Json(name = "data") val data: List<ProxmoxNodeInfo>
)

@JsonClass(generateAdapter = true)
data class ProxmoxNodeInfo(
    @Json(name = "node") val nodeName: String,
    @Json(name = "status") val status: String, // "online", "offline"
    @Json(name = "cpu") val cpuUsagePct: Double,
    @Json(name = "maxcpu") val maxCpuCores: Int,
    @Json(name = "mem") val memoryUsedBytes: Long,
    @Json(name = "maxmem") val maxMemoryBytes: Long,
    @Json(name = "uptime") val uptimeSeconds: Long
)


/**
 * Home Assistant REST API Response Placeholders
 * Commonly fetched from: GET /api/states/sensor.server_power
 */
@JsonClass(generateAdapter = true)
data class HomeAssistantStateResponse(
    @Json(name = "entity_id") val entityId: String,
    @Json(name = "state") val state: String, // e.g., "on", "off", "145.2"
    @Json(name = "attributes") val attributes: HomeAssistantAttributes,
    @Json(name = "last_changed") val lastChanged: String
)

@JsonClass(generateAdapter = true)
data class HomeAssistantAttributes(
    @Json(name = "friendly_name") val friendlyName: String?,
    @Json(name = "unit_of_measurement") val unitOfMeasurement: String?,
    @Json(name = "icon") val icon: String?
)


/**
 * Unraid API Status Response Placeholders
 * Represents main server arrays, parities, and array drive telemetry
 */
@JsonClass(generateAdapter = true)
data class UnraidArrayResponse(
    @Json(name = "status") val arrayStatus: String, // "STARTED", "STOPPED", "PARITY_CHECKING"
    @Json(name = "dirty") val isParitySecured: Boolean,
    @Json(name = "disks") val disks: List<UnraidDiskInfo>
)

@JsonClass(generateAdapter = true)
data class UnraidDiskInfo(
    @Json(name = "id") val diskId: Int,
    @Json(name = "name") val name: String, // e.g., "disk1", "parity"
    @Json(name = "status") val status: String, // "ACTIVE", "SPUN_DOWN"
    @Json(name = "temp") val temperatureCelsius: Double,
    @Json(name = "size") val sizeBytes: Long,
    @Json(name = "free") val freeBytes: Long
)
