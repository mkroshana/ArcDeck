package com.example.ui.dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.HomelabRepository
import com.example.data.repository.ProxmoxRepository
import com.example.data.repository.UnraidRepository
import com.example.data.repository.ArrRepository
import com.example.data.network.UnraidGraphQLClient
import com.squareup.moshi.Moshi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.random.Random
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import javax.net.ssl.HostnameVerifier


class HomelabViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPrefs = application.getSharedPreferences("homelab_settings", Context.MODE_PRIVATE)

    // Persistent API Settings
    val proxmoxUrl = MutableStateFlow(sharedPrefs.getString("proxmox_url", "https://192.168.1.10:8006") ?: "https://192.168.1.10:8006")
    val proxmoxToken = MutableStateFlow(sharedPrefs.getString("proxmox_token", "PVEAPIToken=root@pam!token-name=123456-abc") ?: "PVEAPIToken=root@pam!token-name=123456-abc")
    val proxmoxNode = MutableStateFlow(sharedPrefs.getString("proxmox_node", "pve") ?: "pve")
    
    val unraidUrl = MutableStateFlow(sharedPrefs.getString("unraid_url", "http://192.168.1.15:80/graphql") ?: "http://192.168.1.15:80/graphql")
    val unraidToken = MutableStateFlow(sharedPrefs.getString("unraid_token", "X-Unraid-Secret-Auth") ?: "X-Unraid-Secret-Auth")
    
    val arrUrl = MutableStateFlow(sharedPrefs.getString("arr_url", "http://192.168.1.20:8989") ?: "http://192.168.1.20:8989")
    val arrApiKey = MutableStateFlow(sharedPrefs.getString("arr_api_key", "X-Api-Key-12345-abc") ?: "X-Api-Key-12345-abc")
    
    val useDemoMode = MutableStateFlow(sharedPrefs.getBoolean("demo_mode", true))

    val unraidPoolTypes = MutableStateFlow<Map<String, String>>(emptyMap())

    init {
        unraidPoolTypes.value = loadPoolTypes()
    }

    private fun loadPoolTypes(): Map<String, String> {
        val serialized = sharedPrefs.getString("unraid_pool_types", null) ?: return emptyMap()
        return try {
            serialized.split(",").filter { it.contains(":") }.associate {
                val parts = it.split(":")
                parts[0] to parts[1]
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun savePoolType(poolName: String, poolType: String) {
        val current = unraidPoolTypes.value.toMutableMap()
        if (poolType.isEmpty()) {
            current.remove(poolName)
        } else {
            current[poolName] = poolType
        }
        unraidPoolTypes.value = current
        val serialized = current.entries.joinToString(",") { "${it.key}:${it.value}" }
        sharedPrefs.edit().putString("unraid_pool_types", serialized).apply()
    }

    // OkHttp & Moshi for Clean Architecture network repos
    private val okHttpClient: OkHttpClient = try {
        val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
            override fun getAcceptedIssuers(): Array<X509Certificate> = emptyArray()
        })

        val sslContext = SSLContext.getInstance("SSL")
        sslContext.init(null, trustAllCerts, SecureRandom())

        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .sslSocketFactory(sslContext.socketFactory, trustAllCerts[0] as X509TrustManager)
            .hostnameVerifier(HostnameVerifier { _, _ -> true })
            .build()
    } catch (e: Exception) {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(8, TimeUnit.SECONDS)
            .build()
    }

    private val moshi = Moshi.Builder().build()

    private val proxmoxRepository = ProxmoxRepository(okHttpClient)
    private val unraidRepository = UnraidRepository(UnraidGraphQLClient(okHttpClient, moshi))
    private val arrRepository = ArrRepository(okHttpClient)

    private var fastRefreshJob: Job? = null
    private var mediumRefreshJob: Job? = null
    private var slowRefreshJob: Job? = null

    // API UI States — Proxmox
    private val _proxmoxResources = MutableStateFlow<List<ProxmoxResource>>(emptyList())
    val proxmoxResources = _proxmoxResources.asStateFlow()

    private val _proxmoxNodeStatus = MutableStateFlow<ProxmoxNodeStatus?>(null)
    val proxmoxNodeStatus = _proxmoxNodeStatus.asStateFlow()

    // API UI States — Unraid (real schema)
    private val _unraidArray = MutableStateFlow<UnraidArray?>(null)
    val unraidArray = _unraidArray.asStateFlow()

    private val _unraidSystemInfo = MutableStateFlow<UnraidInfo?>(null)
    val unraidSystemInfo = _unraidSystemInfo.asStateFlow()

    private val _unraidCpuUtil = MutableStateFlow<UnraidCpuUtilization?>(null)
    val unraidCpuUtil = _unraidCpuUtil.asStateFlow()

    private val _unraidMemoryUtil = MutableStateFlow<UnraidMemoryUtilization?>(null)
    val unraidMemoryUtil = _unraidMemoryUtil.asStateFlow()

    private val _unraidDockerContainers = MutableStateFlow<List<UnraidDockerContainer>>(emptyList())
    val unraidDockerContainers = _unraidDockerContainers.asStateFlow()

    private val _unraidVms = MutableStateFlow<List<UnraidVmDomain>>(emptyList())
    val unraidVms = _unraidVms.asStateFlow()

    private val _unraidNotifications = MutableStateFlow<UnraidNotificationOverview?>(null)
    val unraidNotifications = _unraidNotifications.asStateFlow()

    // API UI States — Arr
    private val _arrQueue = MutableStateFlow<List<ArrQueueItem>>(emptyList())
    val arrQueue = _arrQueue.asStateFlow()

    // Loading states
    private val _isLoadingProxmox = MutableStateFlow(false)
    val isLoadingProxmox = _isLoadingProxmox.asStateFlow()

    private val _isLoadingUnraid = MutableStateFlow(false)
    val isLoadingUnraid = _isLoadingUnraid.asStateFlow()

    private val _isLoadingArr = MutableStateFlow(false)
    val isLoadingArr = _isLoadingArr.asStateFlow()

    // Error states
    private val _proxmoxError = MutableStateFlow<String?>(null)
    val proxmoxError = _proxmoxError.asStateFlow()

    private val _unraidError = MutableStateFlow<String?>(null)
    val unraidError = _unraidError.asStateFlow()

    private val _arrError = MutableStateFlow<String?>(null)
    val arrError = _arrError.asStateFlow()

    // Internal tracker DB
    private val repository: HomelabRepository
    val allServices: StateFlow<List<HomelabService>>
    val recentMetrics: StateFlow<List<SystemMetricLog>>
    val terminalSystemLogs: StateFlow<List<TerminalLog>>

    // Dynamic metrics parameters
    private val _currentCpu = MutableStateFlow(42)
    val currentCpu = _currentCpu.asStateFlow()

    private val _currentRam = MutableStateFlow(58)
    val currentRam = _currentRam.asStateFlow()

    private val _currentStorage = MutableStateFlow(73)
    val currentStorage = _currentStorage.asStateFlow()

    private val _netDown = MutableStateFlow(12.4)
    val netDown = _netDown.asStateFlow()

    private val _netUp = MutableStateFlow(2.7)
    val netUp = _netUp.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = HomelabRepository(database.homelabDao())

        allServices = repository.allServices.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        recentMetrics = repository.recentMetrics.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        terminalSystemLogs = repository.terminalLogs.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        // Prepopulate database default list if empty
        viewModelScope.launch {
            if (repository.getServiceCount() == 0) {
                val defaults = listOf(
                    HomelabService(name = "Proxmox VE Cluster", url = "192.168.1.10", type = "PROXMOX", isOnline = true, port = 8006, pingMs = 4, uptimePct = 99.85),
                    HomelabService(name = "Unraid Main Storage", url = "192.168.1.15", type = "UNRAID", isOnline = true, port = 80, pingMs = 6, uptimePct = 99.98),
                    HomelabService(name = "Home Assistant Core", url = "192.168.1.30", type = "HOMEAX", isOnline = true, port = 8123, pingMs = 12, uptimePct = 99.92),
                    HomelabService(name = "Pi-hole Network DNS", url = "192.168.1.4, 192.168.1.5", type = "PIHOLE", isOnline = true, port = 80, pingMs = 3, uptimePct = 100.00),
                    HomelabService(name = "Plex Media Server", url = "192.168.1.40", type = "PLEX", isOnline = false, port = 32400, pingMs = 85, uptimePct = 98.43),
                    HomelabService(name = "Portainer Dashboard", url = "192.168.1.20", type = "PORTAINER", isOnline = true, port = 9000, pingMs = 9, uptimePct = 99.70)
                )
                for (srv in defaults) {
                    repository.insertService(srv)
                }
                repository.insertTerminalLog(TerminalLog(timestamp = System.currentTimeMillis() - 600000, level = "INFO", source = "SYS", message = "System initialization routine launched."))
                repository.insertTerminalLog(TerminalLog(timestamp = System.currentTimeMillis() - 500000, level = "INFO", source = "SYS", message = "Verified local database integrity. Loaded 6 standard services."))
            }
        }

        // Active periodically refreshed dashboard loader
        startDataRefreshLoops()
    }

    private fun cancelDataRefreshLoops() {
        fastRefreshJob?.cancel()
        mediumRefreshJob?.cancel()
        slowRefreshJob?.cancel()
    }

    /**
     * Periodically refresh independent data loops at different frequencies
     */
    private fun startDataRefreshLoops() {
        cancelDataRefreshLoops()

        // 1. Fast loop: telemetry (CPU/RAM utilization + Network simulator) - every 1.0 second
        fastRefreshJob = viewModelScope.launch {
            while (true) {
                refreshFastTelemetry()
                delay(1000)
            }
        }

        // 2. Medium loop: Array overview, Proxmox VMs, Arr queue, Notifications - every 5 seconds
        mediumRefreshJob = viewModelScope.launch {
            while (true) {
                refreshMediumData()
                delay(5000)
            }
        }

        // 3. Slow loop: System info, Docker list, VM list - every 15 seconds
        slowRefreshJob = viewModelScope.launch {
            while (true) {
                refreshSlowData()
                delay(15000)
            }
        }
    }

    suspend fun refreshFastTelemetry() = withContext(Dispatchers.IO) {
        val url = unraidUrl.value
        val token = unraidToken.value
        val demo = useDemoMode.value

        // Fetch CPU & RAM utilization
        val utilResult = unraidRepository.getCpuAndMemoryUtilization(url, token, demo)
        utilResult.onSuccess { (cpu, mem) ->
            _unraidCpuUtil.value = cpu
            _unraidMemoryUtil.value = mem
        }
        // Fetch Proxmox Node Status
        val pveStatusResult = proxmoxRepository.getNodeStatus(
            baseUrl = proxmoxUrl.value,
            token = proxmoxToken.value,
            node = proxmoxNode.value,
            useDemoFallback = demo
        )
        pveStatusResult.onSuccess {
            _proxmoxNodeStatus.value = it
        }
        // Network simulation
        val down = 1.0 + Random.nextDouble(1.0, 150.0)
        val up = 0.5 + Random.nextDouble(0.1, 15.0)
        _netDown.value = String.format(java.util.Locale.US, "%.1f", down).toDouble()
        _netUp.value = String.format(java.util.Locale.US, "%.1f", up).toDouble()

        // Fallback simulated CPU if no utilization yet
        if (_unraidCpuUtil.value == null) {
            _currentCpu.value = Random.nextInt(15, 60)
        }

        // Trigger initial demo data population if needed
        if (demo) {
            if (_proxmoxResources.value.isEmpty()) {
                _proxmoxResources.value = proxmoxRepository.getResources("", "", "", true).getOrDefault(emptyList())
            }
            if (_unraidArray.value == null) {
                _unraidArray.value = unraidRepository.getArrayOverview("", "", true).getOrNull()
            }
            if (_arrQueue.value.isEmpty()) {
                _arrQueue.value = arrRepository.getActiveQueue("", "", true).getOrDefault(emptyList())
            }
            if (_unraidSystemInfo.value == null) {
                _unraidSystemInfo.value = unraidRepository.getSystemInfo("", "", true).getOrNull()
            }
            if (_unraidDockerContainers.value.isEmpty()) {
                _unraidDockerContainers.value = unraidRepository.getDockerContainers("", "", true).getOrDefault(emptyList())
            }
            if (_unraidVms.value.isEmpty()) {
                _unraidVms.value = unraidRepository.getVMs("", "", true).getOrDefault(emptyList())
            }
            if (_unraidNotifications.value == null) {
                _unraidNotifications.value = unraidRepository.getNotificationOverview("", "", true).getOrNull()
            }
        }

        // Recompute aggregate metrics
        calculateAggregateMetrics()
    }

    suspend fun refreshMediumData() = withContext(Dispatchers.IO) {
        val url = unraidUrl.value
        val token = unraidToken.value
        val demo = useDemoMode.value

        // Proxmox Resources
        val pveResult = proxmoxRepository.getResources(
            baseUrl = proxmoxUrl.value,
            token = proxmoxToken.value,
            node = proxmoxNode.value,
            useDemoFallback = demo
        )
        pveResult.onSuccess {
            _proxmoxResources.value = it
            _proxmoxError.value = null
        }.onFailure {
            _proxmoxError.value = it.localizedMessage ?: "Connection Timeout"
            repository.insertTerminalLog(TerminalLog(timestamp = System.currentTimeMillis(), level = "ERROR", source = "PROXMOX", message = "PVE API Error: ${it.message}"))
        }

        // Unraid Array Overview
        val arrayResult = unraidRepository.getArrayOverview(url, token, demo)
        arrayResult.onSuccess {
            _unraidArray.value = it
            _unraidError.value = null
        }.onFailure {
            _unraidError.value = it.localizedMessage ?: "GraphQL Query Failure"
            repository.insertTerminalLog(TerminalLog(timestamp = System.currentTimeMillis(), level = "ERROR", source = "UNRAID", message = "Unraid GraphQL Error: ${it.message}"))
        }

        // Unraid Notifications
        unraidRepository.getNotificationOverview(url, token, demo).onSuccess {
            _unraidNotifications.value = it
        }

        // Arr Queue
        val arrResult = arrRepository.getActiveQueue(
            baseUrl = arrUrl.value,
            apiKey = arrApiKey.value,
            useDemoFallback = demo
        )
        arrResult.onSuccess {
            _arrQueue.value = it
            _arrError.value = null
        }.onFailure {
            _arrError.value = it.localizedMessage ?: "Arr REST Response Timeout"
            repository.insertTerminalLog(TerminalLog(timestamp = System.currentTimeMillis(), level = "ERROR", source = "ARR", message = "Arr API Error: ${it.message}"))
        }
    }

    suspend fun refreshSlowData() = withContext(Dispatchers.IO) {
        val url = unraidUrl.value
        val token = unraidToken.value
        val demo = useDemoMode.value

        // Unraid System Info
        unraidRepository.getSystemInfo(url, token, demo).onSuccess {
            _unraidSystemInfo.value = it
        }

        // Unraid Docker Containers
        unraidRepository.getDockerContainers(url, token, demo).onSuccess {
            _unraidDockerContainers.value = it
        }

        // Unraid VMs
        unraidRepository.getVMs(url, token, demo).onSuccess {
            _unraidVms.value = it
        }
    }

    suspend fun refreshAllRepositories() {
        _isLoadingProxmox.value = true
        _isLoadingUnraid.value = true
        _isLoadingArr.value = true
        _proxmoxError.value = null
        _unraidError.value = null
        _arrError.value = null

        // Cancel background loops to prevent concurrent requests during force refresh
        cancelDataRefreshLoops()

        try {
            refreshFastTelemetry()
            refreshMediumData()
            refreshSlowData()
        } finally {
            _isLoadingProxmox.value = false
            _isLoadingUnraid.value = false
            _isLoadingArr.value = false

            // Restart the loops after force sync completes
            startDataRefreshLoops()
        }
    }

    private fun calculateAggregateMetrics() {
        // Calculate Dynamic Metrics from Proxmox and Unraid
        val pmList = _proxmoxResources.value
        val array = _unraidArray.value
        val cpuUtil = _unraidCpuUtil.value
        val memUtil = _unraidMemoryUtil.value

        val pveNode = _proxmoxNodeStatus.value

        // RAM: prefer Proxmox node status, fallback to Unraid, fallback to VM aggregate
        if (pveNode != null && pveNode.maxmem > 0) {
            val percentage = (pveNode.mem.toDouble() / pveNode.maxmem.toDouble()) * 100
            _currentRam.value = percentage.toInt().coerceIn(1, 99)
        } else if (memUtil != null && memUtil.total > 0) {
            _currentRam.value = memUtil.percentTotal.toInt().coerceIn(5, 99)
        } else if (pmList.isNotEmpty()) {
            val totalRam = pmList.sumOf { it.maxMemory }
            val usedRam = pmList.sumOf { it.memoryUsed }
            if (totalRam > 0) {
                _currentRam.value = ((usedRam.toDouble() / totalRam.toDouble()) * 100).toInt().coerceIn(10, 99)
            }
        }

        // CPU: prefer Proxmox node status, fallback to Unraid CPU
        if (pveNode != null) {
            val percentage = pveNode.cpu * 100
            _currentCpu.value = percentage.toInt().coerceIn(1, 99)
        } else if (cpuUtil != null) {
            _currentCpu.value = cpuUtil.percentTotal.toInt().coerceIn(1, 99)
        }

        // Storage: derive from array capacity (kilobytes)
        if (array?.capacity?.kilobytes != null) {
            val cap = array.capacity!!.kilobytes!!
            val totalKb = cap.total.toLongOrNull() ?: 0L
            val usedKb = cap.used.toLongOrNull() ?: 0L
            if (totalKb > 0) {
                _currentStorage.value = ((usedKb.toDouble() / totalKb.toDouble()) * 100).toInt().coerceIn(1, 99)
            }
        }
    }

    fun executeProxmoxPowerAction(resource: ProxmoxResource, action: String) {
        viewModelScope.launch {
            repository.insertTerminalLog(TerminalLog(
                timestamp = System.currentTimeMillis(),
                level = "INFO",
                source = "PROXMOX",
                message = "Executing power state constraint: '$action' on vmid ${resource.vmid} (${resource.name})."
            ))
            
            val result = proxmoxRepository.performPowerAction(
                baseUrl = proxmoxUrl.value,
                token = proxmoxToken.value,
                node = proxmoxNode.value,
                resource = resource,
                action = action,
                useDemoFallback = useDemoMode.value
            )
            
            result.onSuccess { success ->
                if (success) {
                    val targetStatus = when (action.lowercase()) {
                        "start" -> "running"
                        "stop", "shutdown" -> "stopped"
                        else -> resource.status
                    }
                    
                    // Optimistic update
                    _proxmoxResources.value = _proxmoxResources.value.map {
                        if (it.vmid == resource.vmid) {
                            it.copy(status = targetStatus)
                        } else it
                    }
                    
                    repository.insertTerminalLog(TerminalLog(
                        timestamp = System.currentTimeMillis(),
                        level = "INFO",
                        source = "PROXMOX",
                        message = "Command completed: VM ${resource.vmid} is transitioning to status '$targetStatus'"
                    ))
                } else {
                    repository.insertTerminalLog(TerminalLog(
                        timestamp = System.currentTimeMillis(),
                        level = "ERROR",
                        source = "PROXMOX",
                        message = "PVE API failed to process command task ID."
                    ))
                }
            }.onFailure {
                repository.insertTerminalLog(TerminalLog(
                    timestamp = System.currentTimeMillis(),
                    level = "ERROR",
                    source = "PROXMOX",
                    message = "Power Action failed: ${it.message}"
                ))
            }
        }
    }

    fun updateSettings(
        pveUrl: String, pveToken: String, pveNode: String,
        unUrl: String, unToken: String,
        aUrl: String, aKey: String,
        demo: Boolean
    ) {
        viewModelScope.launch {
            sharedPrefs.edit()
                .putString("proxmox_url", pveUrl)
                .putString("proxmox_token", pveToken)
                .putString("proxmox_node", pveNode)
                .putString("unraid_url", unUrl)
                .putString("unraid_token", unToken)
                .putString("arr_url", aUrl)
                .putString("arr_api_key", aKey)
                .putBoolean("demo_mode", demo)
                .apply()

            proxmoxUrl.value = pveUrl
            proxmoxToken.value = pveToken
            proxmoxNode.value = pveNode
            unraidUrl.value = unUrl
            unraidToken.value = unToken
            arrUrl.value = aUrl
            arrApiKey.value = aKey
            useDemoMode.value = demo

            repository.insertTerminalLog(TerminalLog(
                timestamp = System.currentTimeMillis(),
                level = "INFO",
                source = "SYS",
                message = "Homelab Client updated configuration. Demo Mode: $demo."
            ))

            refreshAllRepositories()
        }
    }

    private fun startTelemetrySimulation() {
        // Redundant, merged into fast telemetry loops
    }

    // Unchanged interactive DB actions for general logging/service list compatibility
    fun toggleServiceStatus(service: HomelabService, isOnline: Boolean) {
        viewModelScope.launch {
            repository.updateServiceStatus(service.id, isOnline)
            val statusStr = if (isOnline) "ONLINE" else "OFFLINE"
            repository.insertTerminalLog(TerminalLog(timestamp = System.currentTimeMillis(), level = if (isOnline) "INFO" else "WARN", source = "SYS", message = "Admin configured state to $statusStr."))
        }
    }

    fun addNewService(name: String, url: String, type: String, port: Int) {
        viewModelScope.launch {
            val srv = HomelabService(name = name, url = url, type = type.uppercase(), isOnline = true, port = port, pingMs = Random.nextInt(2, 15))
            repository.insertService(srv)
        }
    }

    fun deleteService(service: HomelabService) {
        viewModelScope.launch {
            repository.deleteService(service)
        }
    }

    fun clearLogs() {
        viewModelScope.launch {
            repository.clearAllTerminalLogs()
        }
    }
}

class HomelabViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomelabViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomelabViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
