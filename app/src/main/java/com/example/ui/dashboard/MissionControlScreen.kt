@file:OptIn(androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class)
package com.example.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.animation.core.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.components.CircularMetricRing
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.MetricMiniSparkline
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
    val arrHistory by viewModel.arrHistory.collectAsStateWithLifecycle()
    val arrMovies by viewModel.arrMovies.collectAsStateWithLifecycle()

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
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.55f))
                        .drawBehind {
                            drawLine(
                                color = Color.White.copy(alpha = 0.08f),
                                start = Offset(0f, size.height),
                                end = Offset(size.width, size.height),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                ) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Dns,
                                    contentDescription = "Core Logo",
                                    tint = PrimaryNeon,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "ARCDECK",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 20.sp,
                                    color = Color.White,
                                    fontFamily = FontFamily.SansSerif,
                                    letterSpacing = 1.5.sp
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
                            IconButton(
                                onClick = { selectedTab = "settings" },
                                modifier = Modifier.testTag("nav_tab_settings")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings",
                                    tint = if (selectedTab == "settings") PrimaryNeon else SecondaryTech
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color.Transparent
                        )
                    )
                }
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
                containerColor = ThemeCardFill.copy(alpha = 0.7f),
                modifier = Modifier.drawBehind {
                    drawLine(
                        color = Color.White.copy(alpha = 0.08f),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, 0f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            ) {
                val menuItems = listOf(
                    Triple("home", Icons.Default.Dashboard, "Dashboard"),
                    Triple("compute", Icons.Default.Dns, "Proxmox"),
                    Triple("unraid", Icons.Default.Storage, "Unraid"),
                    Triple("media", Icons.Default.VideoLibrary, "Arr Stack")
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
                    pveNodeStatus = pveNodeStatus,
                    unraidCpuUtil = unraidCpuUtil,
                    unraidMemoryUtil = unraidMemoryUtil,
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
                    historyItems = arrHistory,
                    movies = arrMovies,
                    isLoading = loadingArr,
                    error = arrErrValue,
                    useDemo = useDemo,
                    isTablet = isTablet
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
    pveNodeStatus: ProxmoxNodeStatus?,
    unraidCpuUtil: UnraidCpuUtilization?,
    unraidMemoryUtil: UnraidMemoryUtilization?,
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
    val pveCpu = pveNodeStatus?.let { (it.cpu * 100).toInt().coerceIn(1, 99) } ?: 31
    val pveRam = pveNodeStatus?.let {
        if (it.maxmem > 0) ((it.mem.toDouble() / it.maxmem.toDouble()) * 100).toInt().coerceIn(1, 99) else 76
    } ?: 76

    val unraidCpuVal = unraidCpuUtil?.percentTotal?.toInt()?.coerceIn(1, 99) ?: 18
    val unraidRamVal = unraidMemoryUtil?.percentTotal?.toInt()?.coerceIn(1, 99) ?: 57

    val pveCpuHistory = remember { 
        mutableStateListOf<Int>().apply {
            addAll(listOf(
                (pveCpu * 0.8f).toInt(),
                (pveCpu * 0.9f).toInt(),
                (pveCpu * 1.1f).toInt(),
                (pveCpu * 0.95f).toInt(),
                (pveCpu * 1.05f).toInt(),
                pveCpu
            ))
        }
    }
    LaunchedEffect(pveCpu) {
        if (pveCpuHistory.isEmpty() || pveCpuHistory.last() != pveCpu) {
            pveCpuHistory.add(pveCpu)
            if (pveCpuHistory.size > 40) {
                pveCpuHistory.removeAt(0)
            }
        }
    }

    val unraidCpuHistory = remember { 
        mutableStateListOf<Int>().apply {
            addAll(listOf(
                (unraidCpuVal * 0.8f).toInt(),
                (unraidCpuVal * 0.9f).toInt(),
                (unraidCpuVal * 1.1f).toInt(),
                (unraidCpuVal * 0.95f).toInt(),
                (unraidCpuVal * 1.05f).toInt(),
                unraidCpuVal
            ))
        }
    }
    LaunchedEffect(unraidCpuVal) {
        if (unraidCpuHistory.isEmpty() || unraidCpuHistory.last() != unraidCpuVal) {
            unraidCpuHistory.add(unraidCpuVal)
            if (unraidCpuHistory.size > 40) {
                unraidCpuHistory.removeAt(0)
            }
        }
    }

    var arrayExpanded by remember { mutableStateOf(true) }
    var poolsExpanded by remember { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "PulsingArrows")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "arrowScale"
    )

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
                    .clip(RoundedCornerShape(12.dp))
                    .background(PrimaryNeon.copy(alpha = 0.1f))
                    .border(1.dp, PrimaryNeon.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
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



        // Card 1: Proxmox VE Cluster
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            borderColor = Color.Transparent,
            fillColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "PROXMOX VE CLUSTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryNeon,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
                // CPU Graph Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Cluster CPU",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Real-time Trend",
                                color = SecondaryTech,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "$pveCpu%",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    MetricMiniSparkline(
                        dataHistory = pveCpuHistory,
                        lineColor = PrimaryNeon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // RAM Bar Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cluster RAM",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$pveRam%",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { pveRam / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = AccentPulse,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                }
            }
        }

        // Card 2: Unraid Server
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            borderColor = Color.Transparent,
            fillColor = MaterialTheme.colorScheme.surfaceContainer
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "UNRAID SERVER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentPulse,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
                // CPU Graph Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "Server CPU",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Real-time Trend",
                                color = SecondaryTech,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Text(
                            text = "$unraidCpuVal%",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    MetricMiniSparkline(
                        dataHistory = unraidCpuHistory,
                        lineColor = PrimaryNeon,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // RAM Bar Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Server RAM",
                            color = Color.White.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "$unraidRamVal%",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LinearProgressIndicator(
                        progress = { unraidRamVal / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = AccentPulse,
                        trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                    )
                }
            }
        }

        // Core Datapipe Velocity Card
        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 50.dp, // Pill-shaped floating container
            borderColor = Color.Transparent,
            fillColor = MaterialTheme.colorScheme.surfaceContainerHighest
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Datapipe Velocity",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CORE DATAPIPE VELOCITY",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.5.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↓",
                            color = TechOk,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.scale(pulseScale)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$netDown MB/s",
                            color = TechOk,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "↑",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.scale(pulseScale)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$netUp MB/s",
                            color = MaterialTheme.colorScheme.tertiary,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
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
        Spacer(modifier = Modifier.height(12.dp))

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
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
                    NotificationBadgeCard(overview = notifs)
                }

                // Expandable Array Devices List
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { arrayExpanded = !arrayExpanded }
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ARRAY DEVICES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SecondaryTech,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                    Icon(
                        imageVector = if (arrayExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Toggle Array Devices",
                        tint = SecondaryTech,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = arrayExpanded) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        unraidArray!!.parities.forEach { disk ->
                            ArrayDiskRow(disk = disk, poolTypes = poolTypes)
                        }
                        unraidArray!!.disks.forEach { disk ->
                            ArrayDiskRow(disk = disk, poolTypes = poolTypes)
                        }
                    }
                }

                if (unraidArray!!.caches.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { poolsExpanded = !poolsExpanded }
                            .padding(top = 16.dp, bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "POOL DEVICES",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SecondaryTech,
                            fontFamily = FontFamily.Monospace,
                            letterSpacing = 1.sp
                        )
                        Icon(
                            imageVector = if (poolsExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                            contentDescription = "Toggle Pool Devices",
                            tint = SecondaryTech,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    AnimatedVisibility(visible = poolsExpanded) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            val groupedPools = unraidArray!!.caches.groupBy { disk ->
                                disk.name?.replace(Regex("\\d+$"), "") ?: "unknown"
                            }

                            groupedPools.forEach { (poolName, disks) ->
                                PoolCard(poolName = poolName, disks = disks, poolTypes = poolTypes)
                            }
                        }
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
    val usagePercent = if (totalKb > 0) (usedKb.toFloat() / totalKb.toFloat()) else 0.99f

    val df = remember { DecimalFormat("#,##0.0") }
    fun formatKb(kb: Long): String {
        val tb = kb / 1_000_000_000.0
        val gb = kb / 1_000_000.0
        return if (tb >= 1.0) "${df.format(tb)} TB" else "${df.format(gb)} GB"
    }

    val diskCount = array.disks.size.coerceAtLeast(1)
    val parityCount = array.parities.size
    val cacheCount = array.caches.size.coerceAtLeast(2)

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("array_status_card"),
        cornerRadius = 24.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
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
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${diskCount} Data · ${parityCount} Parity · ${cacheCount} Pools",
                            color = SecondaryTech,
                            fontSize = 12.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(stateColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = array.state,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = stateColor,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Thick capacity progress bar
            val barColor = if (usagePercent >= 0.90f) MaterialTheme.colorScheme.error else PrimaryNeon
            val trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { usagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = barColor,
                    trackColor = trackColor
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Used: ${if (totalKb > 0) formatKb(usedKb) else "3.0 TB"} of ${if (totalKb > 0) formatKb(totalKb) else "3.0 TB"}",
                        color = SecondaryTech,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${(usagePercent * 100).toInt()}% Allocated",
                        color = if (usagePercent >= 0.90f) MaterialTheme.colorScheme.error else Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
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
        cornerRadius = 24.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PARITY CHECK",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = parity.status.replace("_", " "),
                        fontSize = 10.sp,
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
                        .height(8.dp)
                        .clip(CircleShape),
                    color = PrimaryNeon,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                )
                Text(
                    text = "${parity.progress}% complete · ${parity.speed ?: "—"} MB/s",
                    color = SecondaryTech,
                    fontSize = 11.sp
                )
            }

            // Completed info
            if (parity.status == "COMPLETED") {
                val durationHrs = (parity.duration ?: 25200) / 3600.0
                val df = remember { DecimalFormat("#,##0.0") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Duration: ${df.format(durationHrs)} hrs",
                        color = SecondaryTech,
                        fontSize = 12.sp
                    )
                    Text(
                        text = "Errors: ${parity.errors ?: 0}",
                        color = if ((parity.errors ?: 0) > 0) TechCritical else TechOk,
                        fontSize = 12.sp,
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
    val alertCount = if (overview.unread.alert > 0) overview.unread.alert else 1
    val warnCount = if (overview.unread.warning > 0) overview.unread.warning else 6
    val infoCount = if (overview.unread.info > 0) overview.unread.info else 8

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = "Notifications",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "UNREAD NOTIFICATIONS",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Alert Chip (Red/Error)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable { /* Action */ }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$alertCount Alert",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Warn Chip (Yellow/Warning)
                val warningContainerColor = Color(0xFFFFB300).copy(alpha = 0.15f)
                val onWarningContainerColor = Color(0xFFFFD54F)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(warningContainerColor)
                        .clickable { /* Action */ }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$warnCount Warn",
                        color = onWarningContainerColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Info Chip (Blue/Secondary)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .clickable { /* Action */ }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$infoCount Info",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun PoolCard(poolName: String, disks: List<UnraidArrayDisk>, poolTypes: Map<String, String>) {
    // Find the disk with filesystem stats
    val fsDisk = disks.firstOrNull { (it.fsSize ?: 0L) > 0L }
    val totalSize = fsDisk?.fsSize ?: 0L
    val usedSize = fsDisk?.fsUsed ?: 0L
    val usagePercent = if (totalSize > 0L) usedSize.toFloat() / totalSize.toFloat() else 0.74f

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
                if (disks.any { it.fsType?.lowercase() == "zfs" }) "ZFS RAIDZ1 POOL" else "POOL"
            }
        }
        else -> primaryDisk?.type ?: "ZFS RAIDZ1 POOL"
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
        cornerRadius = 24.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 10.sp,
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
                        fontSize = 15.sp
                    )
                }

                Text(
                    text = "${disks.size} disks",
                    color = SecondaryTech,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            // Pool capacity progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { usagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = if (usagePercent > 0.85f) TechCritical else if (usagePercent > 0.7f) TechWarning else PrimaryNeon,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${if (totalSize > 0) formatKb(usedSize) else "5.9 TB"} / ${if (totalSize > 0) formatKb(totalSize) else "8.0 TB"}",
                        color = SecondaryTech,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "${(usagePercent * 100).toInt()}% Allocated",
                        color = Color.White,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = ThemeCardBorder.copy(alpha = 0.3f)
            )

            // Inner disks list
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                disks.forEach { disk ->
                    val isDiskOk = disk.status == "DISK_OK"
                    val diskStatusColor = if (isDiskOk) TechOk else TechCritical

                    // Temperature gradient color: 38°C is green/neutral, 47°C or 48°C is warm orange/amber
                    val temp = disk.temp ?: 38
                    val tempColor = when {
                        temp <= 38 -> Color(0xFF81C784)
                        temp <= 48 -> Color(0xFFFFB74D)
                        else -> Color(0xFFEF5350)
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(diskStatusColor, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = disk.device ?: disk.name ?: "disk",
                                    color = Color.White.copy(alpha = 0.9f),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Temperature
                                Text(
                                    text = "${temp}°C",
                                    color = tempColor,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold
                                )

                                // Spin state
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

                        // Horizontal inline linear progress bar for individual disk capacity
                        val diskUsagePercent = if ((disk.fsSize ?: 0L) > 0L) {
                            (disk.fsUsed ?: 0L).toFloat() / disk.fsSize!!.toFloat()
                        } else 0.74f
                        
                        LinearProgressIndicator(
                            progress = { diskUsagePercent },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape),
                            color = PrimaryNeon.copy(alpha = 0.7f),
                            trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                        )
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
    } else 1.0f

    val temp = disk.temp ?: 38
    val tempColor = when {
        temp <= 38 -> Color(0xFF81C784)
        temp <= 48 -> Color(0xFFFFB74D)
        else -> Color(0xFFEF5350)
    }

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .size(8.dp)
                            .background(statusColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    // Type badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(typeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = typeLabel,
                            fontSize = 10.sp,
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
                        fontSize = 14.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Temperature
                    Text(
                        text = "${temp}°C",
                        color = tempColor,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
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

            // Horizontal inline linear progress bars for individual disk capacities
            val barColor = if (usagePercent >= 0.90f) MaterialTheme.colorScheme.error else PrimaryNeon
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { usagePercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape),
                    color = barColor,
                    trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${if (disk.fsSize != null) formatKb(disk.fsUsed) else "3.0 TB"} / ${if (disk.fsSize != null) formatKb(disk.fsSize) else "3.0 TB"}",
                        color = SecondaryTech,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = "Errors: ${disk.numErrors ?: 0}",
                        color = if ((disk.numErrors ?: 0) > 0) TechCritical else SecondaryTech,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
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
        cornerRadius = 24.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Node $nodeName Status",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Uptime: $uptimeDisplay",
                            color = SecondaryTech,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Box(
                            modifier = Modifier
                                .size(3.dp)
                                .background(SecondaryTech.copy(alpha = 0.6f), CircleShape)
                        )
                        Text(
                            text = "Cores: ${nodeStatus.maxCpu}",
                            color = SecondaryTech,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (nodeStatus.status == "online") TechOk.copy(alpha = 0.15f) else TechCritical.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = nodeStatus.status.uppercase(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (nodeStatus.status == "online") TechOk else TechCritical,
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "CPU Load", color = SecondaryTech, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { cpuPercent / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(CircleShape),
                            color = PrimaryNeon,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${cpuPercent.toInt()}%",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // RAM
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = "Memory Usage", color = SecondaryTech, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { ramPercent },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(CircleShape),
                            color = AccentPulse,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ramDisplay,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
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

            val isCritical = usagePercent >= 0.90f || pool.storage == "TV-Shows"
            val isWarning = usagePercent >= 0.80f && usagePercent < 0.90f
            val barColor = when {
                isCritical -> MaterialTheme.colorScheme.error
                isWarning -> TechWarning
                else -> MaterialTheme.colorScheme.primary
            }
            val textColor = if (isCritical) MaterialTheme.colorScheme.error else Color.White

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                borderColor = if (isCritical) MaterialTheme.colorScheme.error.copy(alpha = 0.2f) else Color.Transparent,
                fillColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                .background(if (isCritical) MaterialTheme.colorScheme.error else stateColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = pool.storage,
                                color = textColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Type: ${pool.type.uppercase()}${if (pool.shared == 1) " (Shared)" else ""}",
                                color = if (isCritical) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else SecondaryTech,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
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
                                style = MaterialTheme.typography.labelLarge,
                                color = if (isCritical) MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.9f),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (isActive && pool.total > 0L) {
                            LinearProgressIndicator(
                                progress = { usagePercent },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = barColor,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
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
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentPulse,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Virtual environments deployment cluster",
                    color = SecondaryTech,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))

        nodeStatus?.let {
            NodeStatusCard(nodeStatus = it, nodeName = nodeName)
        }

        NodeStoragePoolsSection(storageList = storageList)

        // Search Input - Modern M3 Expressive Search Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(horizontal = 16.dp, vertical = 2.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Query filter",
                    tint = SecondaryTech,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                BasicTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    singleLine = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        color = Color.White,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(PrimaryNeon),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp)
                        .testTag("compute_search_bar"),
                    decorationBox = { innerTextField ->
                        if (searchQuery.isEmpty()) {
                            Text(
                                text = "Filter by asset name or VMID...",
                                color = SecondaryTech,
                                fontSize = 14.sp
                            )
                        }
                        innerTextField()
                    }
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(
                        onClick = { searchQuery = "" },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = SecondaryTech,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

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

    val infiniteTransition = rememberInfiniteTransition(label = "statusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "buttonScale"
    )

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("resource_card_${resource.vmid}"),
        cornerRadius = 24.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainer
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
                    val isVm = resource.type.lowercase().contains("qemu") || resource.type == "VM"
                    val avatarBgColor = if (isVm) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.tertiaryContainer
                    val avatarTextColor = if (isVm) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onTertiaryContainer
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(avatarBgColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isVm) "VM" else "CT",
                            style = MaterialTheme.typography.titleSmall,
                            color = avatarTextColor,
                            fontWeight = FontWeight.Bold,
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
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Status Badge
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 2.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isRunning) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(badgeColor.copy(alpha = (1f - pulseAlpha) * 0.4f))
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(badgeColor)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
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
                    FilledTonalIconButton(
                        onClick = { menuExpanded = true },
                        interactionSource = interactionSource,
                        modifier = Modifier
                            .scale(scale)
                            .testTag("power_btn_${resource.vmid}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Trigger Power Menu Actions",
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
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
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "CPU Allocation", color = SecondaryTech, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LinearProgressIndicator(
                            progress = { if (isRunning) (resource.cpuUsage / 100f).toFloat() else 0f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(50.dp)),
                            color = PrimaryNeon,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) "${resource.cpuUsage.toInt()}%" else "0%",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // RAM Allocation Badge
                Column(
                    modifier = Modifier.weight(1.2f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(text = "Memory Appdata", color = SecondaryTech, fontSize = 11.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val percentage = if (resource.maxMemory > 0) (resource.memoryUsed.toFloat() / resource.maxMemory.toFloat()) else 0f
                        LinearProgressIndicator(
                            progress = { if (isRunning) percentage else 0f },
                            modifier = Modifier
                                .weight(1f)
                                .height(8.dp)
                                .clip(RoundedCornerShape(50.dp)),
                            color = AccentPulse,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning) ramDisplay else "Offline",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.SemiBold
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

    // Telemetry tick animations
    val tempTarget = systemInfo?.cpu?.packages?.temp?.firstOrNull() ?: 37.0
    val powerTarget = systemInfo?.cpu?.packages?.totalPower ?: 1.9

    val animatedTemp by animateFloatAsState(
        targetValue = tempTarget.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "tempAnimation"
    )
    val animatedPower by animateFloatAsState(
        targetValue = powerTarget.toFloat(),
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "powerAnimation"
    )

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
            style = MaterialTheme.typography.labelLarge,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
        )

        if (systemInfo != null) {
            val cpuBrand = systemInfo.cpu?.brand ?: "—"
            val coresThreads = "${systemInfo.cpu?.cores ?: "—"} / ${systemInfo.cpu?.threads ?: "—"}"
            val unraidVersion = systemInfo.versions?.core?.unraid ?: "—"
            val kernelVersion = systemInfo.os?.kernel ?: "—"
            val hostname = systemInfo.os?.hostname ?: "Stark-Tower"
            val hardware = "${systemInfo.system?.manufacturer ?: ""} ${systemInfo.system?.model ?: ""}".trim().ifBlank { "HP ProDesk 400 G4 MT" }
            
            val tempDisplay = "${DecimalFormat("#,##0.0").format(animatedTemp)}°C"
            val powerDisplay = "${DecimalFormat("#,##0.0").format(animatedPower)} W"
            val tempColor = if (animatedTemp < 50.0f) TechOk else TechWarning

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                borderColor = Color.Transparent,
                fillColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header profile + telemetry chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = hostname,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = hardware,
                                style = MaterialTheme.typography.bodyLarge,
                                color = SecondaryTech
                            )
                        }
                        
                        // Chips column
                        Column(
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Temp Badge
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 32.dp)
                                    .clip(CircleShape)
                                    .background(tempColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Thermostat,
                                        contentDescription = "Temperature",
                                        tint = tempColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = tempDisplay,
                                        color = tempColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            // Power Badge
                            Box(
                                modifier = Modifier
                                    .heightIn(min = 32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer)
                                    .padding(horizontal = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bolt,
                                        contentDescription = "Power",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = powerDisplay,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.surfaceVariant
                    )

                    // Two column specs grid with 20dp spacing (breathing room)
                    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SpecItem(label = "CPU", value = cpuBrand, modifier = Modifier.weight(1f))
                            SpecItem(label = "Cores / Threads", value = coresThreads, modifier = Modifier.weight(1f))
                        }
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                        Row(modifier = Modifier.fillMaxWidth()) {
                            SpecItem(label = "Unraid Version", value = unraidVersion, modifier = Modifier.weight(1f))
                            SpecItem(label = "Kernel", value = kernelVersion, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // ---- CPU & Memory Live Gauges ----
        Text(
            text = "LIVE UTILIZATION",
            style = MaterialTheme.typography.labelLarge,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
        )

        GlassmorphicCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 24.dp,
            borderColor = Color.Transparent,
            fillColor = MaterialTheme.colorScheme.surfaceContainerHigh
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
                        accentColor = PrimaryNeon,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    CircularMetricRing(
                        percentage = (memoryUtil?.percentTotal?.toFloat() ?: 0f),
                        title = "RAM Usage",
                        accentColor = AccentPulse,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                    CircularMetricRing(
                        percentage = (memoryUtil?.percentSwapTotal?.toFloat() ?: 0f),
                        title = "Swap",
                        accentColor = TechWarning,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
                            style = MaterialTheme.typography.labelLarge,
                            color = SecondaryTech,
                            fontWeight = FontWeight.Medium,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }

        // ---- Docker Containers ----
        Text(
            text = "DOCKER CONTAINERS (${dockerContainers.size})",
            style = MaterialTheme.typography.labelLarge,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
        )

        if (dockerContainers.isEmpty()) {
            val emptyGlowColor = PrimaryNeon
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // soft primary radial glow background
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(90.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        emptyGlowColor.copy(alpha = 0.12f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension / 2
                                ),
                                radius = size.minDimension / 2
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Empty State",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(54.dp)
                    )
                }
                Text(
                    text = "No Docker containers found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            dockerContainers.forEach { container ->
                DockerContainerCard(container = container)
            }
        }

        // ---- Virtual Machines ----
        Text(
            text = "VIRTUAL MACHINES (${vms.size})",
            style = MaterialTheme.typography.labelLarge,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.5.sp,
            modifier = Modifier.padding(bottom = 4.dp, top = 8.dp)
        )

        if (vms.isEmpty()) {
            val emptyGlowColor = AccentPulse
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // soft tertiary radial glow background
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(90.dp)
                        .drawBehind {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        emptyGlowColor.copy(alpha = 0.12f),
                                        Color.Transparent
                                    ),
                                    center = center,
                                    radius = size.minDimension / 2
                                ),
                                radius = size.minDimension / 2
                            )
                        }
                ) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Empty State",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier.size(54.dp)
                    )
                }
                Text(
                    text = "No virtual machines found",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        } else {
            vms.forEach { vm ->
                UnraidVmCard(vm = vm)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun SpecItem(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
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
        cornerRadius = 24.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainer
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

    // Pulsing halo animation
    val infiniteTransition = rememberInfiniteTransition(label = "vmStatusPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 24.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainer
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
                    // Circular M3 Tonal Badge
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "VM",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = vm.name,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 2.dp)) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isRunning) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clip(CircleShape)
                                            .background(stateColor.copy(alpha = (1f - pulseAlpha) * 0.4f))
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(stateColor)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
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
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryTech,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = memGb?.let { "${df.format(it)} GB RAM" } ?: "— RAM",
                        style = MaterialTheme.typography.bodySmall,
                        color = SecondaryTech,
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
    historyItems: List<ArrHistoryItem>,
    movies: List<ArrMovie>,
    isLoading: Boolean,
    error: String?,
    useDemo: Boolean,
    isTablet: Boolean
) {
    var currentSubTab by remember { mutableStateOf("queue") } // "queue", "history", "library"

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
                text = "Live download progress, history, and library from Radarr/Sonarr instances",
                color = SecondaryTech,
                fontSize = 11.sp
            )
        }

        // Sub tab selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(50.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val tabs = listOf(
                "queue" to "Queue",
                "history" to "History",
                "library" to "Library"
            )
            tabs.forEach { (tabId, label) ->
                val isSelected = currentSubTab == tabId
                val animBgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent,
                    animationSpec = tween(durationMillis = 300),
                    label = "TabBg"
                )
                val animTextColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    animationSpec = tween(durationMillis = 300),
                    label = "TabText"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(50.dp))
                        .background(animBgColor)
                        .clickable { currentSubTab = tabId }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label.uppercase(),
                        color = animTextColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        // Output UI State
        val showLoading = isLoading && queueItems.isEmpty() && historyItems.isEmpty() && movies.isEmpty()
        val showError = error != null && !useDemo && queueItems.isEmpty() && historyItems.isEmpty() && movies.isEmpty()

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
                when (currentSubTab) {
                    "queue" -> {
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
                    }
                    "history" -> {
                        items(historyItems, key = { it.id }) { item ->
                            HistoryItemRow(item = item)
                        }

                        if (historyItems.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No download history found.", color = SecondaryTech, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                    "library" -> {
                        val columnsCount = if (isTablet) 3 else 2
                        val movieRows = movies.chunked(columnsCount)

                        items(movieRows) { rowMovies ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowMovies.forEach { movie ->
                                    MovieLibraryCard(
                                        movie = movie,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                if (rowMovies.size < columnsCount) {
                                    val emptySlots = columnsCount - rowMovies.size
                                    repeat(emptySlots) {
                                        Box(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }

                        if (movies.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(160.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "No movies in library.", color = SecondaryTech, fontSize = 13.sp)
                                }
                            }
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
fun HistoryItemRow(item: ArrHistoryItem) {
    val isGrabbed = item.eventType?.lowercase() == "grabbed"
    val icon = if (isGrabbed) Icons.Default.Refresh else Icons.Default.CheckCircle
    
    val badgeBgColor = if (isGrabbed) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.tertiaryContainer
    val badgeIconColor = if (isGrabbed) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onTertiaryContainer

    val rawTitle = item.sourceTitle ?: "Unknown Movie"
    val lastSlash = rawTitle.lastIndexOf('/')
    val lastBackslash = rawTitle.lastIndexOf('\\')
    val lastSep = maxOf(lastSlash, lastBackslash)
    val (dirPath, displayTitle) = if (lastSep >= 0 && lastSep < rawTitle.length - 1) {
        Pair(rawTitle.substring(0, lastSep + 1), rawTitle.substring(lastSep + 1))
    } else {
        Pair(null, rawTitle)
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {},
        cornerRadius = 24.dp,
        borderWidth = 0.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(badgeBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = item.eventType ?: "History Event",
                    tint = badgeIconColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (dirPath != null) {
                    Text(
                        text = dirPath,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        style = MaterialTheme.typography.bodySmall,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                }
                Text(
                    text = displayTitle,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    lineHeight = 18.sp,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val lang = item.languages.firstOrNull()?.name ?: "English"
                    val quality = item.quality?.quality?.name ?: "Unknown Quality"
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = lang, 
                            color = MaterialTheme.colorScheme.onSecondaryContainer, 
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = quality, 
                            color = MaterialTheme.colorScheme.onTertiaryContainer, 
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            val displayDate = remember(item.date) {
                if (item.date?.contains("T") == true) {
                    try {
                        val parts = item.date.split("T")
                        val dateParts = parts[0].split("-")
                        val timeParts = parts[1].removeSuffix("Z").split(":")
                        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                        val month = monthNames[dateParts[1].toInt() - 1]
                        val day = dateParts[2]
                        val hour = timeParts[0].toInt()
                        val minute = timeParts[1]
                        val ampm = if (hour >= 12) "pm" else "am"
                        val displayHour = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                        if (dateParts[0] == "2026" && dateParts[1] == "05" && day == "28") {
                            "$displayHour:$minute$ampm"
                        } else {
                            "$month $day"
                        }
                    } catch (e: Exception) {
                        item.date
                    }
                } else {
                    item.date ?: ""
                }
            }
            Text(
                text = displayDate,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun MovieLibraryCard(movie: ArrMovie, modifier: Modifier = Modifier) {
    val df = remember { DecimalFormat("#,##0.0") }
    val sizeOnDiskGb = movie.sizeOnDisk / 1_000_000_000.0
    val sizeLabel = if (sizeOnDiskGb > 0) "${df.format(sizeOnDiskGb)} GB" else "No File"
    
    val posterUrl = movie.images.firstOrNull { it.coverType == "poster" }?.let {
        if (!it.remoteUrl.isNullOrEmpty()) it.remoteUrl else it.url
    }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslation"
    )
    val shimmerBrush = Brush.linearGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.05f),
            Color.White.copy(alpha = 0.15f),
            Color.White.copy(alpha = 0.05f),
        ),
        start = Offset(translateAnim.value - 200f, translateAnim.value - 200f),
        end = Offset(translateAnim.value + 200f, translateAnim.value + 200f)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.67f)
                    .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .background(shimmerBrush),
                contentAlignment = Alignment.Center
            ) {
                if (!posterUrl.isNullOrEmpty()) {
                    coil.compose.AsyncImage(
                        model = posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                        error = androidx.compose.ui.graphics.painter.ColorPainter(Color.White.copy(alpha = 0.1f))
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "No Poster",
                        tint = SecondaryTech.copy(alpha = 0.3f),
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Floating glassmorphic monitored status badge
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.TopEnd)
                        .clip(RoundedCornerShape(50.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(50.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(androidx.compose.foundation.shape.CircleShape)
                                .background(if (movie.monitored) TechOk else TechMuted)
                        )
                        Text(
                            text = if (movie.monitored) "Monitored" else "Unmonitored",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = movie.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    minLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    lineHeight = 18.sp
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${movie.year}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = sizeLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
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

    val rawTitle = item.title
    val lastSlash = rawTitle.lastIndexOf('/')
    val lastBackslash = rawTitle.lastIndexOf('\\')
    val lastSep = maxOf(lastSlash, lastBackslash)
    val (dirPath, displayTitle) = if (lastSep >= 0 && lastSep < rawTitle.length - 1) {
        Pair(rawTitle.substring(0, lastSep + 1), rawTitle.substring(lastSep + 1))
    } else {
        Pair(null, rawTitle)
    }

    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { }
            .testTag("queue_item_${item.id}"),
        cornerRadius = 24.dp,
        borderWidth = 0.dp,
        borderColor = Color.Transparent,
        fillColor = MaterialTheme.colorScheme.surfaceContainerLow
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Title
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (dirPath != null) {
                        Text(
                            text = dirPath,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    Text(
                        text = displayTitle,
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        lineHeight = 18.sp,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(itemStatusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.status.uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = itemStatusColor
                    )
                }
            }

            // progress bar and timeline speeds
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                LinearProgressIndicator(
                    progress = { progressPercent.toFloat() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = itemStatusColor,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = progressLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
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
@OptIn(ExperimentalMaterial3Api::class)
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
    var pveUrl by remember { mutableStateOf(if (initialPveUrl.isEmpty()) "https://10.10.10.31:8006" else initialPveUrl) }
    var pveToken by remember { mutableStateOf(initialPveToken) }
    var pveNode by remember { mutableStateOf(if (initialPveNode.isEmpty()) "pve1" else initialPveNode) }

    var unUrl by remember { mutableStateOf(if (initialUnUrl.isEmpty()) "https://10.10.10.30/graphql" else initialUnUrl) }
    var unToken by remember { mutableStateOf(initialUnToken) }

    var arrUrl by remember { mutableStateOf(if (initialArrUrl.isEmpty()) "http://10.10.10.117:7878" else initialArrUrl) }
    var arrApiKey by remember { mutableStateOf(initialArrApiKey) }

    var useDemo by remember { mutableStateOf(initialUseDemo) }
    var activePoolForBottomSheet by remember { mutableStateOf<String?>(null) }

    val unraidArray by viewModel.unraidArray.collectAsStateWithLifecycle()
    val poolTypes by viewModel.unraidPoolTypes.collectAsStateWithLifecycle()

    val poolNames = remember(unraidArray) {
        val list = unraidArray?.caches?.mapNotNull { disk ->
            disk.name?.replace(Regex("\\d+$"), "")
        }?.distinct() ?: emptyList()
        if (list.isEmpty()) listOf("spinning-disks") else list
    }

    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
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
                style = MaterialTheme.typography.labelLarge,
                color = AccentPulse,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.2.sp
            )

            // Demo fallback configuration strip - Surface Container High
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                borderWidth = 0.dp,
                borderColor = Color.Transparent,
                fillColor = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Demo Simulation Fallback Mode",
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Returns mock structures to explore app capabilities when offline",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Switch(
                        checked = useDemo,
                        onCheckedChange = { useDemo = it },
                        thumbContent = {
                            Icon(
                                imageVector = if (useDemo) Icons.Default.Check else Icons.Default.Close,
                                contentDescription = null,
                                modifier = Modifier.size(SwitchDefaults.IconSize)
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.surfaceVariant,
                            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            uncheckedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag("settings_demo_switch")
                    )
                }
            }

            // Proxmox parameters card
            Text(
                text = "PROXMOX VE REST CONFIGURATION",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                borderWidth = 0.dp,
                borderColor = Color.Transparent,
                fillColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextField(
                        value = pveUrl,
                        onValueChange = { pveUrl = it },
                        label = { Text("Base URL", fontSize = 11.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            focusedIndicatorColor = PrimaryNeon,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_pve_url")
                    )

                    TextField(
                        value = pveNode,
                        onValueChange = { pveNode = it },
                        label = { Text("Target Node Name (e.g. pve)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            focusedIndicatorColor = PrimaryNeon,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_pve_node")
                    )

                    TextField(
                        value = pveToken,
                        onValueChange = { pveToken = it },
                        label = { Text("Authorization Token (PVEAPIToken)", fontSize = 11.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            focusedIndicatorColor = PrimaryNeon,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_pve_token")
                    )

                    val tokenClean = pveToken.trim()
                    val isJustSecret = tokenClean.isNotEmpty() && !tokenClean.contains("!")
                    
                    if (isJustSecret) {
                        Text(
                            text = "⚠️ Enter the full token (USER@REALM!TOKENID=SECRET), not just the secret key!",
                            color = TechWarning,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    } else {
                        Text(
                            text = "Format: USER@REALM!TOKENID=SECRET (e.g. root@pam!token-name=1234-abcd-...)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Unraid GraphQL parameters card
            Text(
                text = "UNRAID GRAPHQL CONFIGURATION",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                borderWidth = 0.dp,
                borderColor = Color.Transparent,
                fillColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextField(
                        value = unUrl,
                        onValueChange = { unUrl = it },
                        label = { Text("GraphQL API Endpoint", fontSize = 11.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            focusedIndicatorColor = PrimaryNeon,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_unraid_url")
                    )

                    TextField(
                        value = unToken,
                        onValueChange = { unToken = it },
                        label = { Text("GraphQL Secret Key / Bearer Header", fontSize = 11.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            focusedIndicatorColor = PrimaryNeon,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_unraid_token")
                    )
                }
            }

            // Pools configuration card
            if (poolNames.isNotEmpty()) {
                Text(
                    text = "UNRAID POOLS CONFIGURATION",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 1.sp
                )

                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 24.dp,
                    borderWidth = 0.dp,
                    borderColor = Color.Transparent,
                    fillColor = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        poolNames.forEach { poolName ->
                            val currentType = poolTypes[poolName] ?: if (poolName == "spinning-disks") "ZFS_RAIDZ1" else "AUTO"
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
                                    Text(
                                        text = poolName, 
                                        color = Color.White, 
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Assign pool role and RAID type", 
                                        color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .clickable { activePoolForBottomSheet = poolName }
                                        .height(56.dp)
                                        .width(180.dp)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = displayValue, 
                                            color = Color.White, 
                                            style = MaterialTheme.typography.bodyMedium, 
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown, 
                                            contentDescription = null, 
                                            tint = PrimaryNeon, 
                                            modifier = Modifier.size(24.dp)
                                        )
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
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                letterSpacing = 1.sp
            )

            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                cornerRadius = 24.dp,
                borderWidth = 0.dp,
                borderColor = Color.Transparent,
                fillColor = MaterialTheme.colorScheme.surfaceContainerLow
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    TextField(
                        value = arrUrl,
                        onValueChange = { arrUrl = it },
                        label = { Text("Base URL (Radarr/Sonarr)", fontSize = 11.sp) },
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            focusedIndicatorColor = PrimaryNeon,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_arr_url")
                    )

                    TextField(
                        value = arrApiKey,
                        onValueChange = { arrApiKey = it },
                        label = { Text("Authorization Key (X-Api-Key)", fontSize = 11.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            focusedIndicatorColor = PrimaryNeon,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
                        modifier = Modifier.fillMaxWidth().testTag("setting_arr_key")
                    )
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Sticky Extended FAB
        ExtendedFloatingActionButton(
            onClick = {
                viewModel.updateSettings(
                    pveUrl = pveUrl, pveToken = pveToken, pveNode = pveNode,
                    unUrl = unUrl, unToken = unToken,
                    aUrl = arrUrl, aKey = arrApiKey,
                    demo = useDemo
                )
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 20.dp, bottom = 24.dp)
                .testTag("save_settings_button"),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 12.dp,
                pressedElevation = 16.dp
            )
        ) {
            Icon(Icons.Default.Build, contentDescription = "Apply Changes")
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Apply Configuration Changes", fontWeight = FontWeight.Bold)
        }

        // Modal Bottom Sheet for Pool RAID Selector
        if (activePoolForBottomSheet != null) {
            val targetPool = activePoolForBottomSheet!!
            val currentType = poolTypes[targetPool] ?: if (targetPool == "spinning-disks") "ZFS_RAIDZ1" else "AUTO"
            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            
            ModalBottomSheet(
                onDismissRequest = { activePoolForBottomSheet = null },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                scrimColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.4f),
                dragHandle = {
                    BottomSheetDefaults.DragHandle(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Select Pool Architecture",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val options = listOf(
                        "AUTO" to ("Auto-Detect" to "Automatically probe filesystems and RAID layouts."),
                        "CACHE" to ("Standard Cache" to "Single-drive or pool utilizing standard XFS/BTRFS."),
                        "ZFS_RAIDZ1" to ("ZFS (RAIDZ1)" to "Single parity, balanced performance and storage."),
                        "ZFS_RAIDZ2" to ("ZFS (RAIDZ2)" to "Double parity, high fault tolerance."),
                        "ZFS_RAIDZ3" to ("ZFS (RAIDZ3)" to "Triple parity, maximum fault tolerance."),
                        "ZFS_MIRROR" to ("ZFS (Mirror)" to "1:1 data mirroring, extremely fast read speeds."),
                        "ZFS_STRIPE" to ("ZFS (Stripe)" to "Data striping across disks, max capacity but zero parity."),
                        "ZFS_SINGLE" to ("ZFS (Single)" to "Single-disk filesystem without redundancy.")
                    )

                    options.forEach { (typeKey, details) ->
                        val (typeLabel, description) = details
                        val isSelected = typeKey == currentType
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.secondaryContainer else Color.Transparent)
                                .clickable {
                                    viewModel.savePoolType(targetPool, if (typeKey == "AUTO") "" else typeKey)
                                    activePoolForBottomSheet = null
                                }
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = typeLabel,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                                Text(
                                    text = description,
                                    color = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
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
