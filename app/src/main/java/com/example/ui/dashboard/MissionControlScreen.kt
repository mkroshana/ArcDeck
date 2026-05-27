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
import com.example.data.model.ArrQueueItem
import com.example.data.model.ProxmoxResource
import com.example.data.model.UnraidPoolInfo
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
    val unPools by viewModel.unraidPools.collectAsStateWithLifecycle()
    val arrItems by viewModel.arrQueue.collectAsStateWithLifecycle()

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
                            text = "HOMELAB COMMAND",
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
        },
        bottomBar = {
            NavigationBar(
                containerColor = ThemeCardFill
            ) {
                val menuItems = listOf(
                    Triple("home", Icons.Default.Home, "Dashboard"),
                    Triple("compute", Icons.Default.Settings, "Compute"),
                    Triple("media", Icons.Default.PlayArrow, "Media Queue"),
                    Triple("settings", Icons.Default.Build, "Settings")
                )

                menuItems.forEach { (tabId, icon, label) ->
                    NavigationBarItem(
                        selected = selectedTab == tabId,
                        onClick = { selectedTab = tabId },
                        icon = { Icon(imageVector = icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
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
                    unraidPools = unPools,
                    loadingUnraid = loadingUnraid,
                    unraidError = unraidErr,
                    useDemo = useDemo
                )
                "compute" -> ComputeView(
                    resources = pveResources,
                    isLoading = loadingPve,
                    error = pveErr,
                    nodeName = pveNode,
                    onPowerAction = { resource, action ->
                        viewModel.executeProxmoxPowerAction(resource, action)
                    },
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
// 1. DASHBOARD VIEW (overview of translucent glassmorphism cards)
// ============================================================================
@Composable
fun DashboardView(
    cpu: Int,
    ram: Int,
    storage: Int,
    netDown: Double,
    netUp: Double,
    unraidPools: List<UnraidPoolInfo>,
    loadingUnraid: Boolean,
    unraidError: String?,
    useDemo: Boolean
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

        // Unraid storage array pools overview sub-section
        Text(
            text = "STORAGE ARRAY POOLS (UNRAID)",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = AccentPulse,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.2.sp
        )

        if (loadingUnraid) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (unraidError != null && !useDemo) {
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
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                unraidPools.forEach { pool ->
                    StoragePoolItemCard(pool = pool)
                }
                if (unraidPools.isEmpty()) {
                    Text(
                        text = "No storage pool records registered.",
                        color = SecondaryTech,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun StoragePoolItemCard(pool: UnraidPoolInfo) {
    val totalGb = pool.sizeBytes / 1_000_000_000.0
    val freeGb = pool.freeBytes / 1_000_000_000.0
    val usedGb = totalGb - freeGb

    val df = remember { DecimalFormat("#,##0.0") }
    val totalStr = if (totalGb >= 1000.0) "${df.format(totalGb / 1000.0)} TB" else "${df.format(totalGb)} GB"
    val usedStr = if (usedGb >= 1000.0) "${df.format(usedGb / 1000.0)} TB" else "${df.format(usedGb)} GB"

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = ThemeCardBorder,
        fillColor = ThemeCardFill
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(if (pool.status == "ONLINE") TechOk else TechWarning, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pool.name,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(if (pool.status == "ONLINE") TechOk.copy(alpha = 0.15f) else TechWarning.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = pool.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pool.status == "ONLINE") TechOk else TechWarning
                    )
                }
            }

            // Line usage metrics progress bar
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LinearProgressIndicator(
                    progress = { pool.usagePercent / 100f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50.dp)),
                    color = if (pool.usagePercent > 85f) TechCritical else PrimaryNeon,
                    trackColor = ThemeCardBorder
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Used: $usedStr of $totalStr",
                        color = SecondaryTech,
                        fontSize = 11.sp
                    )
                    Text(
                        text = "${pool.usagePercent.toInt()}% Allocated",
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


// ============================================================================
// 2. COMPUTE MANAGER VIEW (Proxmox Virtual Machines & LXCs)
// ============================================================================
@Composable
fun ComputeView(
    resources: List<ProxmoxResource>,
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
            .padding(horizontal = 16.dp),
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (error != null && !useDemo) {
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
                    Text(text = "Proxmox Cluster API fetch failed.", color = Color.White, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = error, color = SecondaryTech, fontSize = 12.sp, modifier = Modifier.padding(horizontal = 16.dp))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredList, key = { it.vmid }) { resource ->
                    ResourceItemCard(
                        resource = resource,
                        onPowerAction = { action -> onPowerAction(resource, action) }
                    )
                }

                if (filteredList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "No matching virtual resources found.", color = SecondaryTech, fontSize = 13.sp)
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
fun ResourceItemCard(
    resource: ProxmoxResource,
    onPowerAction: (String) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val isRunning = resource.status.lowercase() == "running"
    val badgeColor = if (isRunning) TechOk else TechCritical

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
                                text = if (isRunning) "Running" else "Stopped",
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
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryNeon)
            }
        } else if (error != null && !useDemo) {
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
