package com.example.data.repository

import com.example.data.model.*
import com.example.data.network.UnraidGraphQLClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class UnraidRepository(
    private val graphQLClient: UnraidGraphQLClient
) {

    // =========================================================================
    // 1. Array Overview — state, capacity, disks, parity
    // =========================================================================

    suspend fun getArrayOverview(
        endpointUrl: String,
        authToken: String?,
        useDemoFallback: Boolean = true
    ): Result<UnraidArray> = withContext(Dispatchers.IO) {
        if (useDemoFallback || endpointUrl.isBlank() || endpointUrl.contains("example.com")) {
            return@withContext Result.success(getDemoArray())
        }

        val query = """
            query {
                array {
                    state
                    capacity {
                        kilobytes { free used total }
                        disks { free used total }
                    }
                    disks {
                        id idx name device size status rotational temp
                        numErrors fsSize fsFree fsUsed type fsType color
                        isSpinning transport
                    }
                    caches {
                        id idx name device size status rotational temp
                        numErrors fsSize fsFree fsUsed type fsType color
                        isSpinning transport
                    }
                    parities {
                        id idx name device size status rotational temp
                        numErrors type isSpinning transport
                    }
                    parityCheckStatus {
                        date duration speed status errors progress
                        correcting paused running
                    }
                    boot {
                        id idx name device size status type isSpinning
                    }
                }
            }
        """.trimIndent()

        try {
            val response = graphQLClient.executeQuery(endpointUrl, authToken, query)
            val array = response.data?.array
            if (array != null) Result.success(array)
            else Result.failure(IOException("No array data returned from Unraid GraphQL"))
        } catch (e: Exception) {
            if (useDemoFallback) Result.success(getDemoArray())
            else Result.failure(e)
        }
    }

    // =========================================================================
    // 2. System Info — CPU, memory layout, OS, system, versions
    // =========================================================================

    suspend fun getSystemInfo(
        endpointUrl: String,
        authToken: String?,
        useDemoFallback: Boolean = true
    ): Result<UnraidInfo> = withContext(Dispatchers.IO) {
        if (useDemoFallback || endpointUrl.isBlank()) {
            return@withContext Result.success(getDemoSystemInfo())
        }

        val query = """
            query {
                info {
                    cpu {
                        manufacturer brand cores threads speed
                        packages { totalPower temp }
                    }
                    memory {
                        layout { size type clockSpeed manufacturer }
                    }
                    os { hostname kernel uptime distro release }
                    system { manufacturer model serial }
                    versions {
                        core { unraid api kernel }
                    }
                }
            }
        """.trimIndent()

        try {
            val response = graphQLClient.executeQuery(endpointUrl, authToken, query)
            val info = response.data?.info
            if (info != null) Result.success(info)
            else Result.failure(IOException("No system info returned"))
        } catch (e: Exception) {
            if (useDemoFallback) Result.success(getDemoSystemInfo())
            else Result.failure(e)
        }
    }

    // =========================================================================
    // 3. CPU & Memory Utilization (live telemetry)
    // =========================================================================

    suspend fun getCpuAndMemoryUtilization(
        endpointUrl: String,
        authToken: String?,
        useDemoFallback: Boolean = true
    ): Result<Pair<UnraidCpuUtilization, UnraidMemoryUtilization>> = withContext(Dispatchers.IO) {
        if (useDemoFallback || endpointUrl.isBlank()) {
            return@withContext Result.success(getDemoCpuMemory())
        }

        val query = """
            query {
                metrics {
                    cpu {
                        percentTotal
                        cpus { percentTotal percentUser percentSystem percentIdle }
                    }
                    memory {
                        total used free available percentTotal
                        swapTotal swapUsed swapFree percentSwapTotal
                    }
                }
            }
        """.trimIndent()

        try {
            val response = graphQLClient.executeQuery(endpointUrl, authToken, query)
            val cpu = response.data?.metrics?.cpu
            val mem = response.data?.metrics?.memory
            if (cpu != null && mem != null) Result.success(Pair(cpu, mem))
            else Result.failure(IOException("No utilization data returned"))
        } catch (e: Exception) {
            if (useDemoFallback) Result.success(getDemoCpuMemory())
            else Result.failure(e)
        }
    }

    // =========================================================================
    // 4. Docker Containers
    // =========================================================================

    suspend fun getDockerContainers(
        endpointUrl: String,
        authToken: String?,
        useDemoFallback: Boolean = true
    ): Result<List<UnraidDockerContainer>> = withContext(Dispatchers.IO) {
        if (useDemoFallback || endpointUrl.isBlank()) {
            return@withContext Result.success(getDemoDockerContainers())
        }

        val query = """
            query {
                docker {
                    containers {
                        id names image state status autoStart
                        ports { privatePort publicPort type }
                    }
                }
            }
        """.trimIndent()

        try {
            val response = graphQLClient.executeQuery(endpointUrl, authToken, query)
            val containers = response.data?.docker?.containers
            if (containers != null) Result.success(containers)
            else Result.failure(IOException("No Docker data returned"))
        } catch (e: Exception) {
            if (useDemoFallback) Result.success(getDemoDockerContainers())
            else Result.failure(e)
        }
    }

    // =========================================================================
    // 5. Virtual Machines
    // =========================================================================

    suspend fun getVMs(
        endpointUrl: String,
        authToken: String?,
        useDemoFallback: Boolean = true
    ): Result<List<UnraidVmDomain>> = withContext(Dispatchers.IO) {
        if (useDemoFallback || endpointUrl.isBlank()) {
            return@withContext Result.success(getDemoVMs())
        }

        val query = """
            query {
                vms {
                    domain {
                        id name state
                    }
                }
            }
        """.trimIndent()

        try {
            val response = graphQLClient.executeQuery(endpointUrl, authToken, query)
            val vms = response.data?.vms?.domains
            if (vms != null) Result.success(vms)
            else Result.failure(IOException("No VM data returned"))
        } catch (e: Exception) {
            if (useDemoFallback) Result.success(getDemoVMs())
            else Result.failure(e)
        }
    }

    // =========================================================================
    // 6. Notification Overview
    // =========================================================================

    suspend fun getNotificationOverview(
        endpointUrl: String,
        authToken: String?,
        useDemoFallback: Boolean = true
    ): Result<UnraidNotificationOverview> = withContext(Dispatchers.IO) {
        if (useDemoFallback || endpointUrl.isBlank()) {
            return@withContext Result.success(getDemoNotifications())
        }

        val query = """
            query {
                notifications {
                    overview {
                        unread { info warning alert total }
                        archive { info warning alert total }
                    }
                }
            }
        """.trimIndent()

        try {
            val response = graphQLClient.executeQuery(endpointUrl, authToken, query)
            val overview = response.data?.notifications?.overview
            if (overview != null) Result.success(overview)
            else Result.failure(IOException("No notification data returned"))
        } catch (e: Exception) {
            if (useDemoFallback) Result.success(getDemoNotifications())
            else Result.failure(e)
        }
    }


    // =========================================================================
    // DEMO DATA GENERATORS
    // =========================================================================

    private fun getDemoArray(): UnraidArray = UnraidArray(
        state = "STARTED",
        capacity = UnraidArrayCapacity(
            kilobytes = UnraidCapacity(
                free = "14320000000",  // ~13.3 TB free
                used = "25680000000", // ~23.9 TB used
                total = "40000000000" // ~37.3 TB total
            ),
            disks = UnraidCapacity(free = "2", used = "4", total = "6")
        ),
        disks = listOf(
            UnraidArrayDisk(id = "disk:1", idx = 1, name = "disk1", device = "sdb", size = 8000000000, status = "DISK_OK", rotational = true, temp = 34, numErrors = 0, fsSize = 7814000000, fsFree = 2344200000, fsUsed = 5469800000, type = "DATA", fsType = "xfs", color = "GREEN_ON", isSpinning = true, transport = "sata"),
            UnraidArrayDisk(id = "disk:2", idx = 2, name = "disk2", device = "sdc", size = 8000000000, status = "DISK_OK", rotational = true, temp = 36, numErrors = 0, fsSize = 7814000000, fsFree = 3125600000, fsUsed = 4688400000, type = "DATA", fsType = "xfs", color = "GREEN_ON", isSpinning = true, transport = "sata"),
            UnraidArrayDisk(id = "disk:3", idx = 3, name = "disk3", device = "sdd", size = 12000000000, status = "DISK_OK", rotational = true, temp = 32, numErrors = 0, fsSize = 11718000000, fsFree = 4687200000, fsUsed = 7030800000, type = "DATA", fsType = "xfs", color = "GREEN_ON", isSpinning = false, transport = "sata"),
            UnraidArrayDisk(id = "disk:4", idx = 4, name = "disk4", device = "sde", size = 12000000000, status = "DISK_OK", rotational = true, temp = 35, numErrors = 0, fsSize = 11718000000, fsFree = 4162800000, fsUsed = 7555200000, type = "DATA", fsType = "xfs", color = "GREEN_ON", isSpinning = true, transport = "sata")
        ),
        caches = listOf(
            UnraidArrayDisk(id = "cache:1", idx = 30, name = "cache", device = "nvme0n1", size = 1000000000, status = "DISK_OK", rotational = false, temp = 42, numErrors = 0, fsSize = 976000000, fsFree = 697000000, fsUsed = 279000000, type = "CACHE", fsType = "btrfs", color = "BLUE_ON", isSpinning = false, transport = "nvme"),
            UnraidArrayDisk(id = "cache:2", idx = 31, name = "cache2", device = "nvme1n1", size = 500000000, status = "DISK_OK", rotational = false, temp = 39, numErrors = 0, fsSize = 488000000, fsFree = 341600000, fsUsed = 146400000, type = "CACHE", fsType = "btrfs", color = "BLUE_ON", isSpinning = false, transport = "nvme")
        ),
        parities = listOf(
            UnraidArrayDisk(id = "parity:1", idx = 0, name = "parity", device = "sda", size = 12000000000, status = "DISK_OK", rotational = true, temp = 37, numErrors = 0, type = "PARITY", isSpinning = true, transport = "sata")
        ),
        parityCheckStatus = UnraidParityCheck(
            date = "2026-05-25T02:00:00Z",
            duration = 28800, // 8 hours
            speed = "125.4",
            status = "COMPLETED",
            errors = 0,
            progress = 100,
            correcting = false,
            paused = false,
            running = false
        ),
        boot = UnraidArrayDisk(id = "boot:1", idx = 54, name = "flash", device = "sdf", size = 31457280, status = "DISK_OK", type = "FLASH", isSpinning = false)
    )

    private fun getDemoSystemInfo(): UnraidInfo = UnraidInfo(
        cpu = UnraidInfoCpu(
            manufacturer = "Intel",
            brand = "Intel Core i7-12700K",
            cores = 12,
            threads = 20,
            speed = 3.6,
            packages = UnraidCpuPackages(totalPower = 89.5, temp = listOf(52.0))
        ),
        memory = UnraidInfoMemory(
            layout = listOf(
                UnraidMemoryLayout(size = 16777216, type = "DDR4", clockSpeed = 3200, manufacturer = "Corsair"),
                UnraidMemoryLayout(size = 16777216, type = "DDR4", clockSpeed = 3200, manufacturer = "Corsair")
            )
        ),
        os = UnraidInfoOs(
            hostname = "Tower",
            kernel = "6.1.74-Unraid",
            uptime = "2026-05-20T08:30:00Z",
            distro = "Slackware",
            release = "15.0"
        ),
        system = UnraidInfoSystem(
            manufacturer = "Supermicro",
            model = "X12SPL-F",
            serial = "SM-2026-DEMO"
        ),
        versions = UnraidInfoVersions(
            core = UnraidCoreVersions(
                unraid = "7.0.1",
                api = "2.60.0",
                kernel = "6.1.74"
            )
        )
    )

    private fun getDemoCpuMemory(): Pair<UnraidCpuUtilization, UnraidMemoryUtilization> {
        val cpu = UnraidCpuUtilization(
            percentTotal = 34.7,
            cpus = listOf(
                UnraidCpuLoad(42.1, 28.3, 13.8, 57.9),
                UnraidCpuLoad(18.5, 12.0, 6.5, 81.5),
                UnraidCpuLoad(55.2, 38.1, 17.1, 44.8),
                UnraidCpuLoad(22.8, 15.4, 7.4, 77.2)
            )
        )
        val mem = UnraidMemoryUtilization(
            total = 34359738368,  // 32 GB
            used = 18832424960,  // ~17.5 GB
            free = 8589934592,   // ~8 GB
            available = 15527313408,
            percentTotal = 54.8,
            swapTotal = 8589934592,
            swapUsed = 429496729,
            swapFree = 8160437863,
            percentSwapTotal = 5.0
        )
        return Pair(cpu, mem)
    }

    private fun getDemoDockerContainers(): List<UnraidDockerContainer> = listOf(
        UnraidDockerContainer(
            id = "docker:abc123", names = listOf("plex"), image = "plexinc/pms-docker:latest",
            state = "running", status = "Up 7 days", autoStart = true,
            ports = listOf(UnraidContainerPort(32400, 32400, "TCP"))
        ),
        UnraidDockerContainer(
            id = "docker:def456", names = listOf("sonarr"), image = "linuxserver/sonarr:latest",
            state = "running", status = "Up 7 days", autoStart = true,
            ports = listOf(UnraidContainerPort(8989, 8989, "TCP"))
        ),
        UnraidDockerContainer(
            id = "docker:ghi789", names = listOf("radarr"), image = "linuxserver/radarr:latest",
            state = "running", status = "Up 7 days", autoStart = true,
            ports = listOf(UnraidContainerPort(7878, 7878, "TCP"))
        ),
        UnraidDockerContainer(
            id = "docker:jkl012", names = listOf("nextcloud"), image = "linuxserver/nextcloud:latest",
            state = "running", status = "Up 3 days", autoStart = true,
            ports = listOf(UnraidContainerPort(443, 8443, "TCP"))
        ),
        UnraidDockerContainer(
            id = "docker:mno345", names = listOf("pihole"), image = "pihole/pihole:latest",
            state = "running", status = "Up 14 days", autoStart = true,
            ports = listOf(UnraidContainerPort(53, 53, "UDP"), UnraidContainerPort(80, 8080, "TCP"))
        ),
        UnraidDockerContainer(
            id = "docker:pqr678", names = listOf("homeassistant"), image = "homeassistant/home-assistant:stable",
            state = "stopped", status = "Exited (0) 2 hours ago", autoStart = false,
            ports = listOf(UnraidContainerPort(8123, 8123, "TCP"))
        )
    )

    private fun getDemoVMs(): List<UnraidVmDomain> = listOf(
        UnraidVmDomain(
            id = "vm:win11", name = "Windows 11 Desktop", state = "running",
            vcpuCount = 8, currentMemory = 17179869184, maxMemory = 17179869184
        ),
        UnraidVmDomain(
            id = "vm:ubuntu", name = "Ubuntu Server 24.04", state = "running",
            vcpuCount = 4, currentMemory = 8589934592, maxMemory = 8589934592
        ),
        UnraidVmDomain(
            id = "vm:macos", name = "macOS Sonoma", state = "shutoff",
            vcpuCount = 6, currentMemory = 0, maxMemory = 12884901888
        )
    )

    private fun getDemoNotifications(): UnraidNotificationOverview = UnraidNotificationOverview(
        unread = UnraidNotificationCounts(info = 5, warning = 2, alert = 0, total = 7),
        archive = UnraidNotificationCounts(info = 142, warning = 18, alert = 3, total = 163)
    )
}
