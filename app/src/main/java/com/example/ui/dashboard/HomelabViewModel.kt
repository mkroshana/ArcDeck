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

    // API UI States — Proxmox
    private val _proxmoxResources = MutableStateFlow<List<ProxmoxResource>>(emptyList())
    val proxmoxResources = _proxmoxResources.asStateFlow()

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
        startTelemetrySimulation()
        startDataRefreshLoop()
    }

    /**
     * Periodically refresh independent data repos: Proxmox, Unraid, and Arr
     */
    private fun startDataRefreshLoop() {
        viewModelScope.launch {
            while (true) {
                refreshAllRepositories()
                delay(8000) // load background telemetry/health updates every 8 seconds
            }
        }
    }

    suspend fun refreshAllRepositories() {
        refreshProxmox()
        refreshUnraid()
        refreshArr()
        
        // Dynamically compute global metrics if demo mode or success values exist
        calculateAggregateMetrics()
    }

    private fun calculateAggregateMetrics() {
        // Calculate Dynamic Metrics from Proxmox and Unraid
        val pmList = _proxmoxResources.value
        val array = _unraidArray.value
        val cpuUtil = _unraidCpuUtil.value
        val memUtil = _unraidMemoryUtil.value

        // RAM: prefer Unraid memory utilization, fallback to Proxmox VM aggregate
        if (memUtil != null && memUtil.total > 0) {
            _currentRam.value = memUtil.percentTotal.toInt().coerceIn(5, 99)
        } else if (pmList.isNotEmpty()) {
            val totalRam = pmList.sumOf { it.maxMemory }
            val usedRam = pmList.sumOf { it.memoryUsed }
            if (totalRam > 0) {
                _currentRam.value = ((usedRam.toDouble() / totalRam.toDouble()) * 100).toInt().coerceIn(10, 99)
            }
        }

        // CPU: prefer Unraid CPU utilization
        if (cpuUtil != null) {
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

    suspend fun refreshProxmox() {
        _isLoadingProxmox.value = true
        _proxmoxError.value = null
        val result = proxmoxRepository.getResources(
            baseUrl = proxmoxUrl.value,
            token = proxmoxToken.value,
            node = proxmoxNode.value,
            useDemoFallback = useDemoMode.value
        )
        result.onSuccess {
            _proxmoxResources.value = it
            _isLoadingProxmox.value = false
        }.onFailure {
            _proxmoxError.value = it.localizedMessage ?: "Connection Timeout"
            _isLoadingProxmox.value = false
            repository.insertTerminalLog(TerminalLog(timestamp = System.currentTimeMillis(), level = "ERROR", source = "PROXMOX", message = "PVE API Error: ${it.message}"))
        }
    }

    suspend fun refreshUnraid() {
        _isLoadingUnraid.value = true
        _unraidError.value = null
        val url = unraidUrl.value
        val token = unraidToken.value
        val demo = useDemoMode.value

        try {
            // Fetch all Unraid data in parallel-like sequence
            val arrayResult = unraidRepository.getArrayOverview(url, token, demo)
            arrayResult.onSuccess { _unraidArray.value = it }
                .onFailure { throw it }

            val sysResult = unraidRepository.getSystemInfo(url, token, demo)
            sysResult.onSuccess { _unraidSystemInfo.value = it }

            val utilResult = unraidRepository.getCpuAndMemoryUtilization(url, token, demo)
            utilResult.onSuccess { (cpu, mem) ->
                _unraidCpuUtil.value = cpu
                _unraidMemoryUtil.value = mem
            }

            val dockerResult = unraidRepository.getDockerContainers(url, token, demo)
            dockerResult.onSuccess { _unraidDockerContainers.value = it }

            val vmResult = unraidRepository.getVMs(url, token, demo)
            vmResult.onSuccess { _unraidVms.value = it }

            val notifResult = unraidRepository.getNotificationOverview(url, token, demo)
            notifResult.onSuccess { _unraidNotifications.value = it }

            _isLoadingUnraid.value = false
        } catch (e: Exception) {
            _unraidError.value = e.localizedMessage ?: "GraphQL Query Failure"
            _isLoadingUnraid.value = false
            repository.insertTerminalLog(TerminalLog(timestamp = System.currentTimeMillis(), level = "ERROR", source = "UNRAID", message = "Unraid GraphQL Error: ${e.message}"))
        }
    }

    suspend fun refreshArr() {
        _isLoadingArr.value = true
        _arrError.value = null
        val result = arrRepository.getActiveQueue(
            baseUrl = arrUrl.value,
            apiKey = arrApiKey.value,
            useDemoFallback = useDemoMode.value
        )
        result.onSuccess {
            _arrQueue.value = it
            _isLoadingArr.value = false
        }.onFailure {
            _arrError.value = it.localizedMessage ?: "Arr REST Response Timeout"
            _isLoadingArr.value = false
            repository.insertTerminalLog(TerminalLog(timestamp = System.currentTimeMillis(), level = "ERROR", source = "ARR", message = "Arr API Error: ${it.message}"))
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
        viewModelScope.launch {
            while (true) {
                delay(3000)
                val randCpu = Random.nextInt(15, 60)
                // Only override CPU with random if we don't have real/demo utilization yet
                if (_unraidCpuUtil.value == null) {
                    _currentCpu.value = randCpu
                }

                val down = 1.0 + Random.nextDouble(1.0, 150.0)
                val up = 0.5 + Random.nextDouble(0.1, 15.0)
                _netDown.value = String.format(java.util.Locale.US, "%.1f", down).toDouble()
                _netUp.value = String.format(java.util.Locale.US, "%.1f", up).toDouble()

                // If in demo mode and data is empty, trigger initial load
                if (useDemoMode.value) {
                    if (_proxmoxResources.value.isEmpty()) {
                        _proxmoxResources.value = proxmoxRepository.getResources("", "", "", true).getOrDefault(emptyList())
                    }
                    if (_unraidArray.value == null) {
                        refreshUnraid()
                    }
                    if (_arrQueue.value.isEmpty()) {
                        _arrQueue.value = arrRepository.getActiveQueue("", "", true).getOrDefault(emptyList())
                    }
                }
            }
        }
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
