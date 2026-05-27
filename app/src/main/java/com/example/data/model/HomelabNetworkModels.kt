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
    @Json(name = "type") val type: String = "", // "qemu" (VM) or "lxc" (Container)
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

@JsonClass(generateAdapter = true)
data class ProxmoxNodeStatusResponse(
    @Json(name = "data") val data: ProxmoxNodeStatus
)

@JsonClass(generateAdapter = true)
data class ProxmoxNodeStatus(
    @Json(name = "cpu") val cpu: Double = 0.0,
    @Json(name = "maxcpu") val maxCpu: Int = 1,
    @Json(name = "uptime") val uptime: Long = 0L,
    @Json(name = "status") val status: String = "online",
    @Json(name = "mem") val memRaw: Any? = null,
    @Json(name = "maxmem") val flatMaxmem: Long? = null,
    @Json(name = "memory") val memoryRaw: Any? = null
) {
    val mem: Long
        get() {
            (memoryRaw as? Map<*, *>)?.let { map ->
                (map["used"] as? Number)?.toLong()?.let { return it }
            }
            when (val raw = memRaw) {
                is Number -> return raw.toLong()
                is Map<*, *> -> {
                    (raw["used"] as? Number)?.toLong()?.let { return it }
                }
            }
            return 0L
        }

    val maxmem: Long
        get() {
            (memoryRaw as? Map<*, *>)?.let { map ->
                (map["total"] as? Number)?.toLong()?.let { return it }
            }
            flatMaxmem?.let { return it }
            when (val raw = memRaw) {
                is Map<*, *> -> {
                    (raw["total"] as? Number)?.toLong()?.let { return it }
                }
            }
            return 1024L
        }
}

@JsonClass(generateAdapter = true)
data class ProxmoxStorageResponse(
    @Json(name = "data") val data: List<ProxmoxStorage>
)

@JsonClass(generateAdapter = true)
data class ProxmoxStorage(
    @Json(name = "storage") val storage: String,
    @Json(name = "type") val type: String = "",
    @Json(name = "active") val active: Int = 1,
    @Json(name = "total") val total: Long = 0L,
    @Json(name = "used") val used: Long = 0L,
    @Json(name = "avail") val avail: Long = 0L,
    @Json(name = "shared") val shared: Int = 0
)

// ============================================================================
// UNRAID MODELS — Matches the real Unraid GraphQL API schema
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
    @Json(name = "array") val array: UnraidArray? = null,
    @Json(name = "info") val info: UnraidInfo? = null,
    @Json(name = "docker") val docker: UnraidDocker? = null,
    @Json(name = "vms") val vms: UnraidVms? = null,
    @Json(name = "notifications") val notifications: UnraidNotifications? = null,
    @Json(name = "metrics") val metrics: UnraidMetrics? = null
)

@JsonClass(generateAdapter = true)
data class UnraidMetrics(
    @Json(name = "cpu") val cpu: UnraidCpuUtilization? = null,
    @Json(name = "memory") val memory: UnraidMemoryUtilization? = null
)

// --- Array -------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class UnraidArray(
    @Json(name = "state") val state: String = "STOPPED", // ArrayState enum
    @Json(name = "capacity") val capacity: UnraidArrayCapacity? = null,
    @Json(name = "disks") val disks: List<UnraidArrayDisk> = emptyList(),
    @Json(name = "caches") val caches: List<UnraidArrayDisk> = emptyList(),
    @Json(name = "parities") val parities: List<UnraidArrayDisk> = emptyList(),
    @Json(name = "parityCheckStatus") val parityCheckStatus: UnraidParityCheck? = null,
    @Json(name = "boot") val boot: UnraidArrayDisk? = null
)

@JsonClass(generateAdapter = true)
data class UnraidArrayCapacity(
    @Json(name = "kilobytes") val kilobytes: UnraidCapacity? = null,
    @Json(name = "disks") val disks: UnraidCapacity? = null
)

@JsonClass(generateAdapter = true)
data class UnraidCapacity(
    @Json(name = "free") val free: String = "0",
    @Json(name = "used") val used: String = "0",
    @Json(name = "total") val total: String = "0"
)

@JsonClass(generateAdapter = true)
data class UnraidArrayDisk(
    @Json(name = "id") val id: String = "",
    @Json(name = "idx") val idx: Int = 0,
    @Json(name = "name") val name: String? = null,
    @Json(name = "device") val device: String? = null,
    @Json(name = "size") val size: Long? = null, // KB
    @Json(name = "status") val status: String? = null, // ArrayDiskStatus enum
    @Json(name = "rotational") val rotational: Boolean? = null,
    @Json(name = "temp") val temp: Int? = null,
    @Json(name = "numReads") val numReads: Long? = null,
    @Json(name = "numWrites") val numWrites: Long? = null,
    @Json(name = "numErrors") val numErrors: Long? = null,
    @Json(name = "fsSize") val fsSize: Long? = null, // KB
    @Json(name = "fsFree") val fsFree: Long? = null, // KB
    @Json(name = "fsUsed") val fsUsed: Long? = null, // KB
    @Json(name = "type") val type: String = "DATA", // ArrayDiskType enum
    @Json(name = "fsType") val fsType: String? = null,
    @Json(name = "color") val color: String? = null,
    @Json(name = "isSpinning") val isSpinning: Boolean? = null,
    @Json(name = "comment") val comment: String? = null,
    @Json(name = "transport") val transport: String? = null
)

@JsonClass(generateAdapter = true)
data class UnraidParityCheck(
    @Json(name = "date") val date: String? = null,
    @Json(name = "duration") val duration: Int? = null, // seconds
    @Json(name = "speed") val speed: String? = null, // MB/s
    @Json(name = "status") val status: String = "NEVER_RUN", // ParityCheckStatus enum
    @Json(name = "errors") val errors: Int? = null,
    @Json(name = "progress") val progress: Int? = null, // 0-100
    @Json(name = "correcting") val correcting: Boolean? = null,
    @Json(name = "paused") val paused: Boolean? = null,
    @Json(name = "running") val running: Boolean? = null
)

// --- System Info -------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class UnraidInfo(
    @Json(name = "cpu") val cpu: UnraidInfoCpu? = null,
    @Json(name = "memory") val memory: UnraidInfoMemory? = null,
    @Json(name = "os") val os: UnraidInfoOs? = null,
    @Json(name = "system") val system: UnraidInfoSystem? = null,
    @Json(name = "versions") val versions: UnraidInfoVersions? = null
)

@JsonClass(generateAdapter = true)
data class UnraidInfoCpu(
    @Json(name = "manufacturer") val manufacturer: String? = null,
    @Json(name = "brand") val brand: String? = null,
    @Json(name = "cores") val cores: Int? = null,
    @Json(name = "threads") val threads: Int? = null,
    @Json(name = "speed") val speed: Double? = null, // GHz
    @Json(name = "packages") val packages: UnraidCpuPackages? = null
)

@JsonClass(generateAdapter = true)
data class UnraidCpuPackages(
    @Json(name = "totalPower") val totalPower: Double? = null,
    @Json(name = "temp") val temp: List<Double> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UnraidInfoMemory(
    @Json(name = "layout") val layout: List<UnraidMemoryLayout> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UnraidMemoryLayout(
    @Json(name = "size") val size: Long = 0, // bytes
    @Json(name = "type") val type: String? = null,
    @Json(name = "clockSpeed") val clockSpeed: Int? = null,
    @Json(name = "manufacturer") val manufacturer: String? = null
)

@JsonClass(generateAdapter = true)
data class UnraidInfoOs(
    @Json(name = "hostname") val hostname: String? = null,
    @Json(name = "kernel") val kernel: String? = null,
    @Json(name = "uptime") val uptime: String? = null, // ISO boot time string
    @Json(name = "distro") val distro: String? = null,
    @Json(name = "release") val release: String? = null
)

@JsonClass(generateAdapter = true)
data class UnraidInfoSystem(
    @Json(name = "manufacturer") val manufacturer: String? = null,
    @Json(name = "model") val model: String? = null,
    @Json(name = "serial") val serial: String? = null
)

@JsonClass(generateAdapter = true)
data class UnraidInfoVersions(
    @Json(name = "core") val core: UnraidCoreVersions? = null
)

@JsonClass(generateAdapter = true)
data class UnraidCoreVersions(
    @Json(name = "unraid") val unraid: String? = null,
    @Json(name = "api") val api: String? = null,
    @Json(name = "kernel") val kernel: String? = null
)

// --- CPU & Memory Utilization ------------------------------------------------

@JsonClass(generateAdapter = true)
data class UnraidCpuUtilization(
    @Json(name = "percentTotal") val percentTotal: Double = 0.0,
    @Json(name = "cpus") val cpus: List<UnraidCpuLoad> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UnraidCpuLoad(
    @Json(name = "percentTotal") val percentTotal: Double = 0.0,
    @Json(name = "percentUser") val percentUser: Double = 0.0,
    @Json(name = "percentSystem") val percentSystem: Double = 0.0,
    @Json(name = "percentIdle") val percentIdle: Double = 0.0
)

@JsonClass(generateAdapter = true)
data class UnraidMemoryUtilization(
    @Json(name = "total") val total: Long = 0, // bytes
    @Json(name = "used") val used: Long = 0,
    @Json(name = "free") val free: Long = 0,
    @Json(name = "available") val available: Long = 0,
    @Json(name = "percentTotal") val percentTotal: Double = 0.0,
    @Json(name = "swapTotal") val swapTotal: Long = 0,
    @Json(name = "swapUsed") val swapUsed: Long = 0,
    @Json(name = "swapFree") val swapFree: Long = 0,
    @Json(name = "percentSwapTotal") val percentSwapTotal: Double = 0.0
)

// --- Docker ------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class UnraidDocker(
    @Json(name = "containers") val containers: List<UnraidDockerContainer> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UnraidDockerContainer(
    @Json(name = "id") val id: String = "",
    @Json(name = "names") val names: List<String> = emptyList(),
    @Json(name = "image") val image: String = "",
    @Json(name = "state") val state: String = "exited", // ContainerState enum
    @Json(name = "status") val status: String = "",
    @Json(name = "autoStart") val autoStart: Boolean = false,
    @Json(name = "ports") val ports: List<UnraidContainerPort> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UnraidContainerPort(
    @Json(name = "privatePort") val privatePort: Int? = null,
    @Json(name = "publicPort") val publicPort: Int? = null,
    @Json(name = "type") val type: String? = null // TCP, UDP
)

// --- VMs ---------------------------------------------------------------------

@JsonClass(generateAdapter = true)
data class UnraidVms(
    @Json(name = "domain") val domains: List<UnraidVmDomain> = emptyList()
)

@JsonClass(generateAdapter = true)
data class UnraidVmDomain(
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "state") val state: String = "shutoff", // VmState enum
    @Json(name = "vcpuCount") val vcpuCount: Int? = null,
    @Json(name = "currentMemory") val currentMemory: Long? = null, // bytes
    @Json(name = "maxMemory") val maxMemory: Long? = null
)

// --- Notifications -----------------------------------------------------------

@JsonClass(generateAdapter = true)
data class UnraidNotifications(
    @Json(name = "overview") val overview: UnraidNotificationOverview? = null
)

@JsonClass(generateAdapter = true)
data class UnraidNotificationOverview(
    @Json(name = "unread") val unread: UnraidNotificationCounts = UnraidNotificationCounts(),
    @Json(name = "archive") val archive: UnraidNotificationCounts = UnraidNotificationCounts()
)

@JsonClass(generateAdapter = true)
data class UnraidNotificationCounts(
    @Json(name = "info") val info: Int = 0,
    @Json(name = "warning") val warning: Int = 0,
    @Json(name = "alert") val alert: Int = 0,
    @Json(name = "total") val total: Int = 0
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

@JsonClass(generateAdapter = true)
data class ArrHistoryResponse(
    @Json(name = "page") val page: Int = 1,
    @Json(name = "pageSize") val pageSize: Int = 10,
    @Json(name = "totalRecords") val totalRecords: Int = 0,
    @Json(name = "records") val records: List<ArrHistoryItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ArrHistoryItem(
    @Json(name = "id") val id: Int,
    @Json(name = "movieId") val movieId: Int? = null,
    @Json(name = "sourceTitle") val sourceTitle: String? = null,
    @Json(name = "quality") val quality: ArrHistoryQuality? = null,
    @Json(name = "languages") val languages: List<ArrHistoryLanguage> = emptyList(),
    @Json(name = "date") val date: String? = null,
    @Json(name = "eventType") val eventType: String? = null
)

@JsonClass(generateAdapter = true)
data class ArrHistoryQuality(
    @Json(name = "quality") val quality: ArrHistoryQualityDetails? = null
)

@JsonClass(generateAdapter = true)
data class ArrHistoryQualityDetails(
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class ArrHistoryLanguage(
    @Json(name = "name") val name: String? = null
)

@JsonClass(generateAdapter = true)
data class ArrMovie(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "year") val year: Int = 0,
    @Json(name = "monitored") val monitored: Boolean = true,
    @Json(name = "hasFile") val hasFile: Boolean = false,
    @Json(name = "sizeOnDisk") val sizeOnDisk: Long = 0L,
    @Json(name = "images") val images: List<ArrMovieImage> = emptyList(),
    @Json(name = "added") val added: String? = null
)

@JsonClass(generateAdapter = true)
data class ArrMovieImage(
    @Json(name = "coverType") val coverType: String,
    @Json(name = "url") val url: String,
    @Json(name = "remoteUrl") val remoteUrl: String? = null
)
