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


// ============================================================================
// UNRAID API PLACEHOLDER MODELS
// ============================================================================
// Real Unraid GraphQL models are defined in HomelabNetworkModels.kt.
// This section is intentionally left empty — demo data is generated
// directly in UnraidRepository.kt using the real model classes.

