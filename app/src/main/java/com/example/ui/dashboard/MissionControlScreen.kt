@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.CircularMetricRing
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.*
import kotlinx.coroutines.launch
import java.text.DecimalFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionControlScreen(
    viewModel: HomelabViewModel,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    
    // settings values
    val pveUrl by viewModel.proxmoxUrl.collectAsStateWithLifecycle()
    val pveToken by viewModel.proxmoxToken.collectAsStateWithLifecycle()
    val pveNode by viewModel.proxmoxNode.collectAsStateWithLifecycle()
    
    val unUrl by viewModel.unraidUrl.collectAsStateWithLifecycle()
    val unToken by viewModel.unraidToken.collectAsStateWithLifecycle()
    
    val arrUrlValue by viewModel.arrUrl.collectAsStateWithLifecycle()
    val arrApiKeyVal by viewModel.arrApiKey.collectAsStateWithLifecycle()
    val useDemo by viewModel.useDemoMode.collectAsStateWithLifecycle()

    // api list values
    val pveResources by viewModel.proxmoxResources.collectAsStateWithLifecycle()
    val pveNodeStatus by viewModel.proxmoxNodeStatus.collectAsStateWithLifecycle()
    val pveStorage by viewModel.proxmoxStorage.collectAsStateWithLifecycle()
    val arrItems by viewModel.arrQueue.collectAsStateWithLifecycle()

    // Unraid state flows
    val unraidArray by viewModel.unraidArray.collectAsStateWithLifecycle()
    val unraidPoolTypes by viewModel.unraidPoolTypes.collectAsStateWithLifecycle()
    val unraidSystemInfo by viewModel.unraidSystemInfo.collectAsStateWithLifecycle()
    val unraidCpuUtil by viewModel.unraidCpuUtil.collectAsStateWithLifecycle()
    val unraidMemoryUtil by viewModel.unraidMemoryUtil.collectAsStateWithLifecycle()
    val unraidDockerContainers by viewModel.unraidDockerContainers.collectAsStateWithLifecycle()
    val unraidVms by viewModel.unraidVms.collectAsStateWithLifecycle()
    val unraidNotifications by viewModel.unraidNotifications.collectAsStateWithLifecycle()

    // loadings
    val loadingPve by viewModel.isLoadingProxmox.collectAsStateWithLifecycle()
    val loadingUnraid by viewModel.isLoadingUnraid.collectAsStateWithLifecycle()
    val loadingArr by viewModel.isLoadingArr.collectAsStateWithLifecycle()

    // errors
    val pveErr by viewModel.proxmoxError.collectAsStateWithLifecycle()
    val unraidErr by viewModel.unraidError.collectAsStateWithLifecycle()
    val arrErrValue by viewModel.arrError.collectAsStateWithLifecycle()

    // gauge performance metrics (derived or simulated)
    val cpuVal by viewModel.currentCpu.collectAsStateWithLifecycle()
    val ramVal by viewModel.currentRam.collectAsStateWithLifecycle()
    val storageVal by viewModel.currentStorage.collectAsStateWithLifecycle()
    val netDown by viewModel.netDown.collectAsStateWithLifecycle()
    val netUp by viewModel.netUp.collectAsStateWithLifecycle()

    // UI tab state
    var selectedTab by remember { mutableStateOf("home") }

    val configuration = LocalConfiguration.current
    val isTablet = configuration.screenWidthDp >= 600

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(ThemeBackground),
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Core Logo",
                                tint = PrimaryNeon,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ARCDECK",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                letterSpacing = 1.2.sp
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.refreshAllRepositories()
                                }
                            },
                            modifier = Modifier.testTag("global_refresh_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Force Synchronization",
                                tint = PrimaryNeon
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ThemeBackground
                    )
                )
                if (loadingPve || loadingUnraid || loadingArr) {
                    LinearWavyProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp),
                        color = PrimaryNeon,
                        trackColor = Color.Transparent
                    )
                } else {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = ThemeCardFill
            ) {
                val menuItems = listOf(
                    Triple("home", Icons.Default.Home, "Dashboard"),
                    Triple("compute", Icons.Default.Settings, "Compute"),
                    Triple("unraid", Icons.Default.Star, "Unraid"),
                    Triple("media", Icons.Default.PlayArrow, "Media Queue"),
                    Triple("settings", Icons.Default.Build, "Settings")
                )

                menuItems.forEach { (tabId, icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab == tabId,
                        onClick = { selectedTab = tabId },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryNeon,
                            unselectedIconColor = SecondaryTech,
                            indicatorColor = PrimaryNeon.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("nav_tab_$tabId")
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(ThemeBackground)
        ) {
            // Main views selector
            when (selectedTab) {
                "home" -> DashboardView(
                    cpu = cpuVal,
                    ram = ramVal,
                    storage = storageVal,
                    netDown = netDown,
                    netUp = netUp,
                    unraidArray = unraidArray,
                    unraidNotifications = unraidNotifications,
                    loadingUnraid = loadingUnraid,
                    unraidError = unraidErr,
                    useDemo = useDemo,
                    poolTypes = unraidPoolTypes
                )
                "compute" -> ComputeView(
                    resources = pveResources,
                    nodeStatus = pveNodeStatus,
                    storageList = pveStorage,
                    isLoading = loadingPve,
                    error = pveErr,
                    nodeName = pveNode,
                    onPowerAction = { resource, action ->
                        viewModel.executeProxmoxPowerAction(resource, action)
                    },
                    useDemo = useDemo
                )
                "unraid" -> UnraidDetailView(
                    systemInfo = unraidSystemInfo,
                    cpuUtil = unraidCpuUtil,
                    memoryUtil = unraidMemoryUtil,
                    dockerContainers = unraidDockerContainers,
                    vms = unraidVms,
                    isLoading = loadingUnraid,
                    error = unraidErr,
                    useDemo = useDemo
                )
                "media" -> MediaQueueView(
                    queueItems = arrItems,
                    isLoading = loadingArr,
                    error = arrErrValue,
                    useDemo = useDemo
                )
                "settings" -> SettingsView(
                    viewModel = viewModel,
                    initialPveUrl = pveUrl,
                    initialPveToken = pveToken,
                    initialPveNode = pveNode,
                    initialUnUrl = unUrl,
                    initialUnToken = unToken,
                    initialArrUrl = arrUrlValue,
                    initialArrApiKey = arrApiKeyVal,
                    initialUseDemo = useDemo
                )
            }
        }
    }
}

// ============================================================================
// 1. DASHBOARD VIEW (overview with real Unraid array data)
// ============================================================================
@Composable
fun DashboardView(
    cpu: Int,
    ram: Int,
    storage: Int,
    netDown: Double,
    netUp: Double,
    unraidArray: UnraidArray?,
    unraidNotifications: UnraidNotificationOverview?,
    loadingUnraid: Boolean,
    unraidError: String?,
    useDemo: Boolean,
    poolTypes: Map<String, String> = emptyMap()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Demo banner indicator
        if (useDemo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(PrimaryNeon.copy(alpha = 0.12f))
                    .border(1.dp, PrimaryNeon.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Demo Mode Info",
                        tint = PrimaryNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Running in Demo Fallback Mode. Configure physical API keys in Settings tab.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Section header
        Text(
            text = "SYSTEM TELEMETRY SUMMARY",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        // Triple circle gauge card (translucent glassmorphism card style)
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ThemeCardBorder.copy(alpha = 0.6f),
            fillColor = ThemeCardFill
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CircularMetricRing(
                        percentage = cpu.toFloat(),
                        title = "Cluster CPU",
                        accentColor = PrimaryNeon
                    )
                    CircularMetricRing(
                        percentage = ram.toFloat(),
                        title = "Cluster RAM",
                        accentColor = AccentPulse
                    )
                    CircularMetricRing(
                        percentage = storage.toFloat(),
                        title = "Storage Pool",
                        accentColor = TechWarning
                    )
                }
            }
        }

        // Live Datapipe throughput speed strip
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ThemeCardBorder.copy(alpha = 0.4f),
            fillColor = ThemeCardFill
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Datapipe Velocity",
                        tint = PrimaryNeon,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CORE DATAPIPE VELOCITY",
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "↓ $netDown MB/s",
                        color = TechOk,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "↑ $netUp MB/s",
                        color = PrimaryNeon,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // ==================================================================
        // UNRAID ARRAY STATUS (real schema)
        // ==================================================================
        Text(
            text = "UNRAID ARRAY STATUS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        val showLoading = loadingUnraid && unraidArray == null
        val showError = unraidError != null && !useDemo && unraidArray == null

        if (showLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (showError) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TechCritical.copy(alpha = 0.08f))
                    .border(1.dp, TechCritical.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = TechCritical)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Unraid connection failed: $unraidError", color = Color.White, fontSize = 12.sp)
                }
            }
        } else if (unraidArray != null) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (unraidError != null && !useDemo) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(TechCritical.copy(alpha = 0.1f))
                            .border(1.dp, TechCritical.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Error",
                                tint = TechCritical,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Refresh failed: $unraidError",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Array State & Capacity Card
                ArrayStatusCard(array = unraidArray!!)

                // Parity Check Card
                unraidArray!!.parityCheckStatus?.let { parity ->
                    ParityCheckCard(parity = parity)
                }

                // Notification Badge
                unraidNotifications?.let { notifs ->
                    if (notifs.unread.total > 0) {
                        NotificationBadgeCard(overview = notifs)
                    }
                }

                // Disk List
                Text(
                    text = "ARRAY DEVICES",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = SecondaryTech,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                // Parity disks
                unraidArray!!.parities.forEach { disk ->
                    ArrayDiskRow(disk = disk)
                }
                // Data disks
                unraidArray!!.disks.forEach { disk ->
                    ArrayDiskRow(disk = disk)
                }

                if (unraidArray!!.caches.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "POOL DEVICES",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTech,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )

                    // Group caches by their base name (stripping trailing digits)
                    val groupedPools = unraidArray!!.caches.groupBy { disk ->
                        disk.name?.replace(Regex("\\d+$"), "") ?: "unknown"
                    }

                    groupedPools.forEach { (poolName, disks) ->
                        PoolCard(poolName = poolName, disks = disks, poolTypes = poolTypes)
                    }
                }
            }
        } else {
            Text(
                text = "No array data available.",
                color = SecondaryTech,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}


// ============================================================================
// DASHBOARD — Array Status Card
// ============================================================================
@Composable
fun ArrayStatusCard(array: UnraidArray) {
    val stateColor = when (array.state) {
        "STARTED" -> TechOk
        "STOPPED" -> TechCritical
        else -> TechWarning
    }

    val cap = array.capacity?.kilobytes
    val totalKb = cap?.total?.toLongOrNull() ?: 0L
    val usedKb = cap?.used?.toLongOrNull() ?: 0L
    val freeKb = cap?.free?.toLongOrNull() ?: 0L
    val usagePercent = if (totalKb > 0) (usedKb.toFloat() / totalKb.toFloat()) else 0f

    val df = remember { DecimalFormat("#,##0.0") }
    fun formatKb(kb: Long): String {
        val tb = kb / 1_000_000_000.0
        val gb = kb / 1_000_000.0
        return if (tb >= 1.0) "${df.format(tb)} TB" else "${df.format(gb)} GB"
    }

    val diskCount = array.disks.size
    val parityCount = array.parities.size
    val cacheCount = array.caches.size

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("array_status_card"),
        borderColor = stateColor.copy(alpha = 0.3f),
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: State + Disk summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(stateColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Array ${array.state.replace("_", " ")}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "${diskCount} Data · ${parityCount} Parity · ${cacheCount} Pool" + if (cacheCount != 1) "s" else "",
                            color = SecondaryTech,
                            fontSize = 11.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(stateColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = array.state,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = stateColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Capacity progress bar
            if (totalKb > 0) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { usagePercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50.dp)),
                        color = if (usagePercent > 0.85f) TechCritical else PrimaryNeon,
                        trackColor = ThemeCardBorder
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Used: ${formatKb(usedKb)} of ${formatKb(totalKb)}",
                            color = SecondaryTech,
                            fontSize = 11.sp
                        )
                        Text(
                            text = "${(usagePercent * 100).toInt()}% Allocated",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// DASHBOARD — Parity Check Card
// ============================================================================
@Composable
fun ParityCheckCard(parity: UnraidParityCheck) {
    val isRunning = parity.running == true
    val statusColor = when (parity.status) {
        "COMPLETED" -> TechOk
        "RUNNING" -> PrimaryNeon
        "PAUSED" -> TechWarning
        "FAILED" -> TechCritical
        "CANCELLED" -> TechMuted
        else -> SecondaryTech
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = ThemeCardBorder,
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Parity",
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PARITY CHECK",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = parity.status.replace("_", " "),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            // Progress bar if running
            if (isRunning && parity.progress != null) {
                LinearProgressIndicator(
                    progress = { parity.progress / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = PrimaryNeon,
                    trackColor = ThemeCardBorder
                )
                Text(
                    text = "${parity.progress}% complete · ${parity.speed ?: "—"} MB/s",
                    color = SecondaryTech,
                    fontSize = 11.sp
                )
            }

            // Completed info
            if (parity.status == "COMPLETED") {
                val durationHrs = (parity.duration ?: 0) / 3600.0
                val df = remember { DecimalFormat("#,##0.1") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Duration: ${df.format(durationHrs)} hrs",
                        color = SecondaryTech,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "Errors: ${parity.errors ?: 0}",
                        color = if ((parity.errors ?: 0) > 0) TechCritical else TechOk,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

// ============================================================================
// DASHBOARD — Notification Badge Card
// ============================================================================
@Composable
fun NotificationBadgeCard(overview: UnraidNotificationOverview) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = ThemeCardBorder,
        fillColor = ThemeCardFill
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = if (overview.unread.alert > 0) TechCritical else TechWarning,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "UNREAD NOTIFICATIONS",
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (overview.unread.alert > 0) {
                    NotifCountBadge(count = overview.unread.alert, label = "Alert", color = TechCritical)
                }
                if (overview.unread.warning > 0) {
                    NotifCountBadge(count = overview.unread.warning, label = "Warn", color = TechWarning)
                }
                if (overview.unread.info > 0) {
                    NotifCountBadge(count = overview.unread.info, label = "Info", color = PrimaryNeon)
                }
            }
        }
    }
}

@Composable
fun NotifCountBadge(count: Int, label: String, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$count $label",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun PoolCard(poolName: String, disks: List<UnraidArrayDisk>, poolTypes: Map<String, String>) {
    // Find the disk with filesystem stats
    val fsDisk = disks.firstOrNull { (it.fsSize ?: 0L) > 0L }
    val totalSize = fsDisk?.fsSize ?: 0L
    val usedSize = fsDisk?.fsUsed ?: 0L
    val usagePercent = if (totalSize > 0L) usedSize.toFloat() / totalSize.toFloat() else 0f

    // Status dot color (Check if all disks are OK)
    val allOk = disks.all { it.status == "DISK_OK" }
    val statusColor = if (allOk) TechOk else TechCritical

    // Pool Type Badge Label
    val primaryDisk = disks.firstOrNull()
    val typeLabel = when (primaryDisk?.type) {
        "CACHE" -> {
            val userType = poolTypes[poolName] ?: poolTypes[poolName.lowercase()]
            if (userType != null) {
                userType.replace("_", " ")
            } else {
                if (disks.any { it.fsType?.lowercase() == "zfs" }) "ZFS POOL" else "POOL"
            }
        }
        else -> primaryDisk?.type ?: "POOL"
    }

    val typeColor = PrimaryNeon

    val df = remember { DecimalFormat("#,##0.0") }
    fun formatKb(kb: Long): String {
        val tb = kb / 1_000_000_000.0
        val gb = kb / 1_000_000.0
        return if (tb >= 1.0) "${df.format(tb)} TB" else "${df.format(gb)} GB"
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = ThemeCardBorder.copy(alpha = 0.5f),
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Header: Name + Badge + State
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = poolName,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Text(
                    text = "${disks.size} disk" + if (disks.size > 1) "s" else "",
                    color = SecondaryTech,
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Pool capacity progress bar (if available)
            if (totalSize > 0L) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    LinearProgressIndicator(
                        progress = { usagePercent },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(50.dp)),
                        color = if (usagePercent > 0.85f) TechCritical else if (usagePercent > 0.7f) TechWarning else PrimaryNeon,
                        trackColor = ThemeCardBorder
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatKb(usedSize)} / ${formatKb(totalSize)}",
                            color = SecondaryTech,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Text(
                            text = "${(usagePercent * 100).toInt()}% Allocated",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                val sumSize = disks.sumOf { it.size ?: 0L }
                if (sumSize > 0L) {
                    Text(
                        text = "Total Size: ${formatKb(sumSize)} (No filesystem reported)",
                        color = SecondaryTech,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Divider
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = ThemeCardBorder.copy(alpha = 0.4f)
            )

            // Inner disks list
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                disks.forEach { disk ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(5.dp)
                                    .background(
                                        if (disk.status == "DISK_OK") TechOk else TechCritical,
                                        CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = disk.name ?: "disk${disk.idx}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "(${disk.device ?: "—"})",
                                color = SecondaryTech,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Temperature
                            disk.temp?.let {
                                Text(
                                    text = "${it}°C",
                                    color = if (it > 45) TechWarning else SecondaryTech,
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Spin state
                            if (disk.isSpinning != null) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .background(
                                            if (disk.isSpinning == true) TechOk else TechMuted,
                                            CircleShape
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// DASHBOARD — Array Disk Row
// ============================================================================
@Composable
fun ArrayDiskRow(disk: UnraidArrayDisk, poolTypes: Map<String, String> = emptyMap()) {
    val statusOk = disk.status == "DISK_OK"
    val statusColor = if (statusOk) TechOk else if (disk.status == "DISK_NP") TechMuted else TechCritical

    val typeLabel = when (disk.type) {
        "CACHE" -> {
            val userType = poolTypes[disk.name]
            if (userType != null) {
                userType.replace("_", " ")
            } else {
                if (disk.fsType?.lowercase() == "zfs") "ZFS POOL" else "POOL"
            }
        }
        else -> disk.type
    }

    val typeColor = when (disk.type) {
        "PARITY" -> AccentPulse
        "CACHE" -> PrimaryNeon
        "DATA" -> TechOk
        "FLASH" -> TechWarning
        else -> SecondaryTech
    }

    val df = remember { DecimalFormat("#,##0.0") }
    fun formatKb(kb: Long?): String {
        if (kb == null || kb == 0L) return "—"
        val tb = kb / 1_000_000_000.0
        val gb = kb / 1_000_000.0
        return if (tb >= 1.0) "${df.format(tb)} TB" else "${df.format(gb)} GB"
    }

    val usagePercent = if ((disk.fsSize ?: 0L) > 0L) {
        (disk.fsUsed ?: 0L).toFloat() / disk.fsSize!!.toFloat()
    } else 0f

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = ThemeCardBorder.copy(alpha = 0.5f),
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status dot
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(typeColor.copy(alpha = 0.12f))
                            .padding(horizontal = 6.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = typeColor,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = disk.name ?: "disk${disk.idx}",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Temperature
                    disk.temp?.let {
                        Text(
                            text = "${it}°C",
                            color = if (it > 45) TechWarning else SecondaryTech,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    // Spin indicator
                    if (disk.isSpinning != null) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(
                                    if (disk.isSpinning == true) TechOk else TechMuted,
                                    CircleShape
                                )
                        )
                    }
                }
            }

            // Capacity bar (only for data/cache disks with filesystem)
            if ((disk.fsSize ?: 0L) > 0L) {
                LinearProgressIndicator(
                    progress = { usagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = if (usagePercent > 0.85f) TechCritical else if (usagePercent > 0.7f) TechWarning else PrimaryNeon,
                    trackColor = ThemeCardBorder
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${formatKb(disk.fsUsed)} / ${formatKb(disk.fsSize)}",
                        color = SecondaryTech,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Errors: ${disk.numErrors ?: 0}",
                        color = if ((disk.numErrors ?: 0) > 0) TechCritical else SecondaryTech,
                        fontSize = 10.sp
                    )
                }
            } else {
                // Parity disks — show total size and errors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Size: ${formatKb(disk.size)}",
                        color = SecondaryTech,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Errors: ${disk.numErrors ?: 0}",
                        color = if ((disk.numErrors ?: 0) > 0) TechCritical else SecondaryTech,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}


@Composable
fun NodeStatusCard(nodeStatus: ProxmoxNodeStatus, nodeName: String) {
    val stateColor = if (nodeStatus.status == "online") TechOk else TechCritical

    val memoryGb = nodeStatus.maxmem / 1_000_000_000.0
    val usedGb = nodeStatus.mem / 1_000_000_000.0
    val format = remember { DecimalFormat("#,##0.0") }
    val ramDisplay = "${format.format(usedGb)} / ${format.format(memoryGb)} GB"
    val ramPercent = if (nodeStatus.maxmem > 0) (nodeStatus.mem.toFloat() / nodeStatus.maxmem.toFloat()) else 0f

    val cpuPercent = (nodeStatus.cpu * 100).toFloat()
    
    val uptimeDays = nodeStatus.uptime / 86400.0
    val uptimeHrs = (nodeStatus.uptime % 86400) / 3600.0
    val uptimeDisplay = if (uptimeDays >= 1.0) {
        "${uptimeDays.toInt()}d ${uptimeHrs.toInt()}h"
    } else {
        "${uptimeHrs.toInt()}h"
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("node_status_card"),
        borderColor = stateColor.copy(alpha = 0.3f),
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(stateColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Node $nodeName Status",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Uptime: $uptimeDisplay · Cores: ${nodeStatus.maxCpu}",
                            color = SecondaryTech,
                            fontSize = 11.sp
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(stateColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = nodeStatus.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = stateColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Gauges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // CPU
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "CPU Load", color = SecondaryTech, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { cpuPercent / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50.dp)),
                            color = PrimaryNeon,
                            trackColor = ThemeCardBorder
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${cpuPercent.toInt()}%",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // RAM
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Memory Usage", color = SecondaryTech, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { ramPercent },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50.dp)),
                            color = AccentPulse,
                            trackColor = ThemeCardBorder
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ramDisplay,
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NodeStoragePoolsSection(storageList: List<ProxmoxStorage>) {
    if (storageList.isEmpty()) return

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "NODE STORAGE POOLS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp,
            modifier = Modifier.padding(top = 4.dp)
        )

        storageList.forEach { pool ->
            val isActive = pool.active == 1
            val stateColor = if (isActive) TechOk else TechCritical
            val usagePercent = if (pool.total > 0) (pool.used.toFloat() / pool.total.toFloat()) else 0f

            val totalGb = pool.total / 1_000_000_000.0
            val usedGb = pool.used / 1_000_000_000.0
            val format = remember { DecimalFormat("#,##0.0") }
            val displayCapacity = if (isActive) "${format.format(usedGb)} / ${format.format(totalGb)} GB" else "Offline"

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ThemeCardBorder.copy(alpha = 0.4f),
                fillColor = ThemeCardFill
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(stateColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = pool.storage,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Type: ${pool.type.uppercase()}${if (pool.shared == 1) " (Shared)" else ""}",
                                color = SecondaryTech,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = displayCapacity,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        if (isActive && pool.total > 0L) {
                            LinearProgressIndicator(
                                progress = { usagePercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .clip(RoundedCornerShape(50.dp)),
                                color = if (usagePercent > 0.85f) TechCritical else if (usagePercent > 0.7f) TechWarning else PrimaryNeon,
                                trackColor = ThemeCardBorder
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================================================
// 2. COMPUTE MANAGER VIEW (Proxmox Virtual Machines & LXCs)
// ============================================================================
@Composable
fun ComputeView(
    resources: List<ProxmoxResource>,
    nodeStatus: ProxmoxNodeStatus?,
    storageList: List<ProxmoxStorage>,
    isLoading: Boolean,
    error: String?,
    nodeName: String,
    onPowerAction: (ProxmoxResource, String) -> Unit,
    useDemo: Boolean
) {
    var searchQuery by remember { mutableStateOf("") }
    val filteredList = remember(searchQuery, resources) {
        if (searchQuery.isBlank()) resources
        else resources.filter { it.name.contains(searchQuery, ignoreCase = true) || it.vmid.toString().contains(searchQuery) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Section header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "COMPUTE MANAGER Node: $nodeName",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentPulse,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Virtual environments deployment cluster",
                    color = SecondaryTech,
                    fontSize = 11.sp
                )
            }
        }

        nodeStatus?.let {
            NodeStatusCard(nodeStatus = it, nodeName = nodeName)
        }

        NodeStoragePoolsSection(storageList = storageList)

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Filter by asset name or VMID...", color = SecondaryTech, fontSize = 13.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Query filter", tint = SecondaryTech) },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = PrimaryNeon,
                unfocusedBorderColor = ThemeCardBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("compute_search_bar")
        )

        // State displays
        val showLoading = isLoading && resources.isEmpty()
        val showError = error != null && !useDemo && resources.isEmpty()

        if (showLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (showError) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = TechCritical, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Proxmox Cluster API fetch failed.", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = error, color = SecondaryTech, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        } else {
            if (error != null && !useDemo && resources.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TechCritical.copy(alpha = 0.1f))
                        .border(1.dp, TechCritical.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = TechCritical,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Refresh failed: $error",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filteredList.forEach { resource ->
                    ResourceItemCard(
                        resource = resource,
                        onPowerAction = { action -> onPowerAction(resource, action) }
                    )
                }

                if (filteredList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "No matching virtual resources found.", color = SecondaryTech, fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun ResourceItemCard(
    resource: ProxmoxResource,
    onPowerAction: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val statusClean = resource.status.lowercase()
    val isRunning = statusClean == "running"
    val isPaused = statusClean == "paused" || statusClean == "suspended"
    val isStopped = statusClean == "stopped"

    val badgeColor = when {
        isRunning -> TechOk
        isPaused -> TechWarning
        else -> TechCritical
    }

    val memoryGb = resource.maxMemory / 1_000_000_000.0
    val usedGb = resource.memoryUsed / 1_000_000_000.0
    val format = remember { DecimalFormat("#,##0.0") }
    val ramDisplay = "${format.format(usedGb)} / ${format.format(memoryGb)} GB"

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("resource_card_${resource.vmid}"),
        borderColor = ThemeCardBorder,
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Name, VMID, Category, Status, Power Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    // Category icon/symbol
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.08f))
                            .border(1.dp, badgeColor.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (resource.type.lowercase().contains("qemu") || resource.type == "VM") "VM" else "CT",
                            color = badgeColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = resource.name,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "[ID: ${resource.vmid}]",
                                color = SecondaryTech,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Status Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(badgeColor)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = resource.status.replaceFirstChar { it.uppercase() },
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Power actions menu triggered drop
                Box {
                    IconButton(
                        onClick = { menuExpanded = true },
                        modifier = Modifier.testTag("power_btn_${resource.vmid}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow, // Power action indicator
                            contentDescription = "Trigger Power Menu Actions",
                            tint = PrimaryNeon
                        )
                    }

                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false },
                        modifier = Modifier.background(ThemeCardFill).border(1.dp, ThemeCardBorder)
                    ) {
                        if (isStopped) {
                            DropdownMenuItem(
                                text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(TechOk, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Start", color = Color.White, fontSize = 12.sp)
                                }},
                                onClick = {
                                    onPowerAction("start")
                                    menuExpanded = false
                                }
                            )
                        }
                        if (isRunning) {
                            DropdownMenuItem(
                                text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(PrimaryNeon, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Reboot", color = Color.White, fontSize = 12.sp)
                                }},
                                onClick = {
                                    onPowerAction("reboot")
                                    menuExpanded = false
                                }
                            )
                            val isQemu = resource.type.lowercase().contains("qemu") || resource.type == "VM"
                            if (isQemu) {
                                DropdownMenuItem(
                                    text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).background(TechWarning, CircleShape))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Suspend", color = Color.White, fontSize = 12.sp)
                                    }},
                                    onClick = {
                                        onPowerAction("suspend")
                                        menuExpanded = false
                                    }
                                )
                            }
                            DropdownMenuItem(
                                text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(TechWarning, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Shutdown", color = Color.White, fontSize = 12.sp)
                                }},
                                onClick = {
                                    onPowerAction("shutdown")
                                    menuExpanded = false
                                }
                            )
                        }
                        if (isPaused) {
                            DropdownMenuItem(
                                text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(TechOk, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Resume", color = Color.White, fontSize = 12.sp)
                                }},
                                onClick = {
                                    onPowerAction("resume")
                                    menuExpanded = false
                                }
                            )
                        }
                        if (isRunning || isPaused) {
                            DropdownMenuItem(
                                text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(6.dp).background(TechCritical, CircleShape))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Stop (Force)", color = Color.White, fontSize = 12.sp)
                                }},
                                onClick = {
                                    onPowerAction("stop")
                                    menuExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Stat progress metrics CPU & RAM
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // CPU Core Badge
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "CPU Allocation", color = SecondaryTech, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { if (isRunning) (resource.cpuUsage / 100f).toFloat() else 0f },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50.dp)),
                            color = PrimaryNeon,
                            trackColor = ThemeCardBorder
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "${resource.cpuUsage.toInt()}%" else "0%",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // RAM Allocation Badge
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "Memory Appdata", color = SecondaryTech, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val percentage = if (resource.maxMemory > 0) (resource.memoryUsed.toFloat() / resource.maxMemory.toFloat()) else 0f
                        LinearProgressIndicator(
                            progress = { if (isRunning) percentage else 0f },
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(RoundedCornerShape(50.dp)),
                            color = AccentPulse,
                            trackColor = ThemeCardBorder
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) ramDisplay else "Offline",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


// ============================================================================
// 2.5 UNRAID DETAIL VIEW (new tab)
// ============================================================================
@Composable
fun UnraidDetailView(
    systemInfo: UnraidInfo?,
    cpuUtil: UnraidCpuUtilization?,
    memoryUtil: UnraidMemoryUtilization?,
    dockerContainers: List<UnraidDockerContainer>,
    vms: List<UnraidVmDomain>,
    isLoading: Boolean,
    error: String?,
    useDemo: Boolean
) {
    val hasNoData = systemInfo == null && dockerContainers.isEmpty() && vms.isEmpty()

    if (isLoading && hasNoData) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryNeon)
        }
        return
    }

    if (error != null && !useDemo && hasNoData) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Warning, contentDescription = "Error", tint = TechCritical, modifier = Modifier.size(32.dp))
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = "Unraid API connection failed.", color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = error, color = SecondaryTech, fontSize = 12.sp)
            }
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        if (error != null && !useDemo) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(TechCritical.copy(alpha = 0.1f))
                    .border(1.dp, TechCritical.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Error",
                        tint = TechCritical,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Refresh failed: $error",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // ---- System Info Card ----
        Text(
            text = "SYSTEM INFORMATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        if (systemInfo != null) {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ThemeCardBorder,
                fillColor = ThemeCardFill
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    systemInfo.os?.hostname?.let {
                        SysInfoRow("Hostname", it)
                    }
                    systemInfo.system?.manufacturer?.let { mfg ->
                        SysInfoRow("Hardware", "${mfg} ${systemInfo.system?.model ?: ""}")
                    }
                    systemInfo.cpu?.brand?.let {
                        SysInfoRow("CPU", it)
                    }
                    systemInfo.cpu?.let { cpu ->
                        SysInfoRow("Cores / Threads", "${cpu.cores ?: "—"} / ${cpu.threads ?: "—"}")
                    }
                    systemInfo.versions?.core?.unraid?.let {
                        SysInfoRow("Unraid Version", it)
                    }
                    systemInfo.os?.kernel?.let {
                        SysInfoRow("Kernel", it)
                    }

                    // Memory layout summary
                    if (systemInfo.memory?.layout?.isNotEmpty() == true) {
                        val totalMem = systemInfo.memory!!.layout.sumOf { it.size }
                        val memGb = totalMem / 1_048_576.0
                        val df = DecimalFormat("#,##0.0")
                        val firstSlot = systemInfo.memory!!.layout.first()
                        SysInfoRow("Memory", "${df.format(memGb)} GB ${firstSlot.type ?: ""} @ ${firstSlot.clockSpeed ?: "—"} MHz")
                    }

                    // CPU Package Temp
                    systemInfo.cpu?.packages?.temp?.firstOrNull()?.let { temp ->
                        SysInfoRow("CPU Temp", "${DecimalFormat("#,##0.0").format(temp)}°C")
                    }
                    systemInfo.cpu?.packages?.totalPower?.let { power ->
                        SysInfoRow("CPU Power", "${DecimalFormat("#,##0.0").format(power)} W")
                    }
                }
            }
        }

        // ---- CPU & Memory Live Gauges ----
        Text(
            text = "LIVE UTILIZATION",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ThemeCardBorder.copy(alpha = 0.6f),
            fillColor = ThemeCardFill
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    CircularMetricRing(
                        percentage = (cpuUtil?.percentTotal?.toFloat() ?: 0f),
                        title = "CPU Load",
                        accentColor = PrimaryNeon
                    )
                    CircularMetricRing(
                        percentage = (memoryUtil?.percentTotal?.toFloat() ?: 0f),
                        title = "RAM Usage",
                        accentColor = AccentPulse
                    )
                    CircularMetricRing(
                        percentage = (memoryUtil?.percentSwapTotal?.toFloat() ?: 0f),
                        title = "Swap",
                        accentColor = TechWarning
                    )
                }

                // Memory detail row
                memoryUtil?.let { mem ->
                    val df = DecimalFormat("#,##0.0")
                    val usedGb = mem.used / 1_073_741_824.0
                    val totalGb = mem.total / 1_073_741_824.0
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "RAM: ${df.format(usedGb)} / ${df.format(totalGb)} GB",
                            color = SecondaryTech,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // ---- Docker Containers ----
        Text(
            text = "DOCKER CONTAINERS (${dockerContainers.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        if (dockerContainers.isEmpty()) {
            Text(text = "No Docker containers found.", color = SecondaryTech, fontSize = 12.sp)
        } else {
            dockerContainers.forEach { container ->
                DockerContainerCard(container = container)
            }
        }

        // ---- Virtual Machines ----
        Text(
            text = "VIRTUAL MACHINES (${vms.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        if (vms.isEmpty()) {
            Text(text = "No virtual machines found.", color = SecondaryTech, fontSize = 12.sp)
        } else {
            vms.forEach { vm ->
                UnraidVmCard(vm = vm)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SysInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = SecondaryTech,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}

@Composable
fun DockerContainerCard(container: UnraidDockerContainer) {
    val isRunning = container.state.lowercase() == "running"
    val stateColor = when (container.state.lowercase()) {
        "running" -> TechOk
        "paused" -> TechWarning
        else -> TechCritical
    }
    val displayName = container.names.firstOrNull()?.removePrefix("/") ?: container.id.take(12)

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = ThemeCardBorder.copy(alpha = 0.5f),
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(stateColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = displayName,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = container.image,
                            color = SecondaryTech,
                            fontSize = 10.sp,
                            maxLines = 1
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(stateColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = container.state.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = stateColor
                        )
                    }
                    if (container.autoStart) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "AUTO",
                            fontSize = 8.sp,
                            color = PrimaryNeon.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }

            // Ports row
            if (container.ports.isNotEmpty()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    container.ports.take(3).forEach { port ->
                        val portText = if (port.publicPort != null && port.publicPort != port.privatePort) {
                            "${port.publicPort}:${port.privatePort}/${port.type ?: "TCP"}"
                        } else {
                            "${port.privatePort ?: "—"}/${port.type ?: "TCP"}"
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(ThemeCardBorder)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = portText,
                                fontSize = 9.sp,
                                color = SecondaryTech,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                    if (container.ports.size > 3) {
                        Text(
                            text = "+${container.ports.size - 3} more",
                            fontSize = 9.sp,
                            color = SecondaryTech
                        )
                    }
                }
            }

            // Status text
            Text(
                text = container.status,
                color = SecondaryTech.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
fun UnraidVmCard(vm: UnraidVmDomain) {
    val isRunning = vm.state.lowercase() == "running"
    val stateColor = when (vm.state.lowercase()) {
        "running" -> TechOk
        "paused" -> TechWarning
        else -> TechCritical
    }

    val df = remember { DecimalFormat("#,##0.0") }
    val memGb = vm.maxMemory?.let { it / 1_073_741_824.0 }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = ThemeCardBorder.copy(alpha = 0.5f),
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(stateColor.copy(alpha = 0.08f))
                            .border(1.dp, stateColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VM",
                            color = stateColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = vm.name,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(stateColor, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = vm.state.replaceFirstChar { it.uppercase() },
                                color = stateColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = vm.vcpuCount?.let { "$it vCPU" } ?: "— vCPU",
                        color = SecondaryTech,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = memGb?.let { "${df.format(it)} GB RAM" } ?: "— RAM",
                        color = SecondaryTech,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}


// ============================================================================
// 3. MEDIA QUEUE VIEW (*Arr: Sonarr/Radarr active download progress streams)
// ============================================================================
@Composable
fun MediaQueueView(
    queueItems: List<ArrQueueItem>,
    isLoading: Boolean,
    error: String?,
    useDemo: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        // Section description
        Column {
            Text(
                text = "MEDIA DOWNLOAD QUEUE (*ARR)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = AccentPulse,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Live download progress from Radarr/Sonarr instances",
                color = SecondaryTech,
                fontSize = 11.sp
            )
        }

        // Output UI State
        val showLoading = isLoading && queueItems.isEmpty()
        val showError = error != null && !useDemo && queueItems.isEmpty()

        if (showLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (showError) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = TechCritical, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "Arr client core failed to refresh queue.", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = error, color = SecondaryTech, fontSize = 12.sp)
                }
            }
        } else {
            if (error != null && !useDemo && queueItems.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(TechCritical.copy(alpha = 0.1f))
                        .border(1.dp, TechCritical.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Error",
                            tint = TechCritical,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Refresh failed: $error",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 11.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(queueItems, key = { it.id }) { item ->
                    DownloadQueueItemRow(item = item)
                }

                if (queueItems.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "Download queue is completely clear.", color = SecondaryTech, fontSize = 13.sp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

@Composable
fun DownloadQueueItemRow(item: ArrQueueItem) {
    val isPaused = item.status.lowercase() == "paused"
    val isDone = item.sizeLeft <= 0 || item.status.lowercase() == "completed"
    
    val totalGb = item.size / 1_000_000_000.0
    val leftGb = item.sizeLeft / 1_000_000_000.0
    val doneGb = (totalGb - leftGb).coerceAtLeast(0.0)

    val format = remember { DecimalFormat("#,##0.00") }
    val progressLabel = "${format.format(doneGb)} GB of ${format.format(totalGb)} GB"
    val progressPercent = if (item.size > 0) ((item.size - item.sizeLeft) / item.size) else 1.0

    val itemStatusColor = if (isPaused) TechWarning else if (isDone) TechOk else PrimaryNeon

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("queue_item_${item.id}"),
        borderColor = ThemeCardBorder,
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.title,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        maxLines = 2,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(itemStatusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.status.uppercase(),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = itemStatusColor
                    )
                }
            }

            // progress bar and timeline speeds
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { progressPercent.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = itemStatusColor,
                    trackColor = ThemeCardBorder
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressLabel,
                        color = SecondaryTech,
                        fontSize = 11.sp
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Protocol tag (Usenet or Torrent)
                        if (!item.protocol.isNullOrEmpty()) {
                            Text(
                                text = item.protocol.uppercase(),
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Time left
                        Text(
                            text = item.timeLeft ?: "Completed",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}


// ============================================================================
// 4. CONFIGURATIONS SETTINGS VIEW (To update header placeholders and urls)
// ============================================================================
@Composable
fun SettingsView(
    viewModel: HomelabViewModel,
    initialPveUrl: String,
    initialPveToken: String,
    initialPveNode: String,
    initialUnUrl: String,
    initialUnToken: String,
    initialArrUrl: String,
    initialArrApiKey: String,
    initialUseDemo: Boolean
) {
    var pveUrl by remember { mutableStateOf(initialPveUrl) }
    var pveToken by remember { mutableStateOf(initialPveToken) }
    var pveNode by remember { mutableStateOf(initialPveNode) }

    var unUrl by remember { mutableStateOf(initialUnUrl) }
    var unToken by remember { mutableStateOf(initialUnToken) }

    var arrUrl by remember { mutableStateOf(initialArrUrl) }
    var arrApiKey by remember { mutableStateOf(initialArrApiKey) }

    var useDemo by remember { mutableStateOf(initialUseDemo) }

    val unraidArray by viewModel.unraidArray.collectAsStateWithLifecycle()
    val poolTypes by viewModel.unraidPoolTypes.collectAsStateWithLifecycle()

    val poolNames = remember(unraidArray) {
        unraidArray?.caches?.mapNotNull { disk ->
            disk.name?.replace(Regex("\\d+$"), "")
        }?.distinct() ?: emptyList()
    }

    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "HARDWARE ENDPOINTS CREDENTIALS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        // Demo fallback configuration strip
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ThemeCardBorder,
            fillColor = ThemeCardFill
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Demo Simulation Fallback Mode",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Returns visually rich mock structures to explore app capabilities when offline",
                        color = SecondaryTech,
                        fontSize = 11.sp
                    )
                }

                Switch(
                    checked = useDemo,
                    onCheckedChange = { useDemo = it },
                    modifier = Modifier.testTag("settings_demo_switch")
                )
            }
        }

        // Proxmox parameters card
        Text(
            text = "PROXMOX VE REST CONFIGURATION",
            fontSize = 10.sp,
            color = SecondaryTech,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ThemeCardBorder,
            fillColor = ThemeCardFill
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = pveUrl,
                    onValueChange = { pveUrl = it },
                    label = { Text("Base URL", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon, unfocusedBorderColor = ThemeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setting_pve_url")
                )

                OutlinedTextField(
                    value = pveNode,
                    onValueChange = { pveNode = it },
                    label = { Text("Target Node Name (e.g. pve)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon, unfocusedBorderColor = ThemeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setting_pve_node")
                )

                OutlinedTextField(
                    value = pveToken,
                    onValueChange = { pveToken = it },
                    label = { Text("Authorization Token (PVEAPIToken)", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon, unfocusedBorderColor = ThemeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setting_pve_token")
                )

                val tokenClean = pveToken.trim()
                val isJustSecret = tokenClean.isNotEmpty() && !tokenClean.contains("!")
                
                if (isJustSecret) {
                    Text(
                        text = "⚠️ Enter the full token (USER@REALM!TOKENID=SECRET), not just the secret key!",
                        color = TechWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Format: USER@REALM!TOKENID=SECRET (e.g. root@pam!token-name=1234-abcd-...)",
                        color = SecondaryTech.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Unraid GraphQL parameters card
        Text(
            text = "UNRAID GRAPHQL CONFIGURATION",
            fontSize = 10.sp,
            color = SecondaryTech,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ThemeCardBorder,
            fillColor = ThemeCardFill
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = unUrl,
                    onValueChange = { unUrl = it },
                    label = { Text("GraphQL API Endpoint", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon, unfocusedBorderColor = ThemeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setting_unraid_url")
                )

                OutlinedTextField(
                    value = unToken,
                    onValueChange = { unToken = it },
                    label = { Text("GraphQL Secret Key / Bearer Header", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon, unfocusedBorderColor = ThemeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setting_unraid_token")
                )
            }
        }

        // Pools configuration card
        if (poolNames.isNotEmpty()) {
            Text(
                text = "UNRAID POOLS CONFIGURATION",
                fontSize = 10.sp,
                color = SecondaryTech,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = ThemeCardBorder,
                fillColor = ThemeCardFill
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    poolNames.forEach { poolName ->
                        var expanded by remember { mutableStateOf(false) }
                        val currentType = poolTypes[poolName] ?: "AUTO"
                        val options = listOf(
                            "AUTO" to "Auto-Detect",
                            "CACHE" to "Standard Cache",
                            "ZFS_RAIDZ1" to "ZFS (RAIDZ1)",
                            "ZFS_RAIDZ2" to "ZFS (RAIDZ2)",
                            "ZFS_RAIDZ3" to "ZFS (RAIDZ3)",
                            "ZFS_MIRROR" to "ZFS (Mirror)",
                            "ZFS_STRIPE" to "ZFS (Stripe)",
                            "ZFS_SINGLE" to "ZFS (Single)"
                        )
                        val displayValue = options.find { it.first == currentType }?.second ?: currentType

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = poolName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(text = "Assign pool role and RAID type", color = SecondaryTech, fontSize = 10.sp)
                            }

                            Box {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(ThemeCardBorder)
                                        .clickable { expanded = true }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = displayValue, color = PrimaryNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = PrimaryNeon, modifier = Modifier.size(16.dp))
                                    }
                                }

                                DropdownMenu(
                                    expanded = expanded,
                                    onDismissRequest = { expanded = false },
                                    modifier = Modifier.background(ThemeCardFill).border(1.dp, ThemeCardBorder, RoundedCornerShape(6.dp))
                                ) {
                                    options.forEach { (typeKey, typeLabel) ->
                                        DropdownMenuItem(
                                            text = { Text(typeLabel, color = Color.White, fontSize = 12.sp) },
                                            onClick = {
                                                viewModel.savePoolType(poolName, if (typeKey == "AUTO") "" else typeKey)
                                                expanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // *Arr server queue parameters card
        Text(
            text = "SERVICES MANAGER (*ARR) CONFIGURATION",
            fontSize = 10.sp,
            color = SecondaryTech,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            borderColor = ThemeCardBorder,
            fillColor = ThemeCardFill
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = arrUrl,
                    onValueChange = { arrUrl = it },
                    label = { Text("Base URL (Radarr/Sonarr)", fontSize = 12.sp) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon, unfocusedBorderColor = ThemeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setting_arr_url")
                )

                OutlinedTextField(
                    value = arrApiKey,
                    onValueChange = { arrApiKey = it },
                    label = { Text("Authorization Key (X-Api-Key)", fontSize = 12.sp) },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White, unfocusedTextColor = Color.White,
                        focusedBorderColor = PrimaryNeon, unfocusedBorderColor = ThemeCardBorder
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("setting_arr_key")
                )
            }
        }

        // Save command parameters buttons
        Button(
            onClick = {
                viewModel.updateSettings(
                    pveUrl = pveUrl, pveToken = pveToken, pveNode = pveNode,
                    unUrl = unUrl, unToken = unToken,
                    aUrl = arrUrl, aKey = arrApiKey,
                    demo = useDemo
                )
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryNeon,
                contentColor = ThemeBackground
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
                .height(48.dp)
                .testTag("save_settings_button")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Build, contentDescription = "Publish Key")
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Apply Configuration Changes", fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(26.dp))
    }
}

/**
 * Scale Helper
 */
fun Modifier.scale(scale: Float): Modifier = this.then(
    layout { measurable, constraints ->
        val placeable = measurable.measure(constraints)
        layout(
            (placeable.width * scale).toInt(),
            (placeable.height * scale).toInt()
        ) {
            placeable.placeRelativeWithLayer(0, 0) {
                scaleX = scale
                scaleY = scale
            }
        }
    }
)
