package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FuelDatabase
import com.example.data.FuelLog
import com.example.data.FuelRepository
import com.example.data.ServiceLog
import com.example.ui.*
import com.example.ui.components.AddServiceDialog
import com.example.ui.components.BackupRestoreDialog
import com.example.ui.components.PersianDatePickerDialog
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.ServicesScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.PersianDateHelper
import java.text.SimpleDateFormat
import java.util.*

enum class AppScreen {
    FUEL,
    SERVICES,
    REPORTS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                android.graphics.Color.TRANSPARENT,
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.light(
                android.graphics.Color.WHITE,
                android.graphics.Color.WHITE
            )
        )

        val database = FuelDatabase.getDatabase(this)
        val repository = FuelRepository(database.fuelLogDao(), database.serviceLogDao())

        setContent {
            MyApplicationTheme {
                val viewModel: FuelViewModel = viewModel(
                    factory = FuelViewModelFactory(repository)
                )
                FuelTrackerApp(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelTrackerApp(viewModel: FuelViewModel, modifier: Modifier = Modifier) {
    val uiStateLogs by viewModel.uiLogsList.collectAsStateWithLifecycle()
    val servicesList by viewModel.servicesList.collectAsStateWithLifecycle()
    val stats by viewModel.statistics.collectAsStateWithLifecycle()
    val serviceStats by viewModel.serviceStatistics.collectAsStateWithLifecycle()
    val reportsData by viewModel.reportsData.collectAsStateWithLifecycle()
    val selectedPeriod by viewModel.reportPeriod.collectAsStateWithLifecycle()
    val customDateRange by viewModel.customDateRange.collectAsStateWithLifecycle()
    val unitSystem by viewModel.unitSystem.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf(AppScreen.FUEL) }
    var showAddFuelDialog by remember { mutableStateOf(false) }
    var showAddServiceDialog by remember { mutableStateOf(false) }
    var showBackupRestoreDialog by remember { mutableStateOf(false) }
    var logToEdit by remember { mutableStateOf<FuelLog?>(null) }
    var serviceToEdit by remember { mutableStateOf<ServiceLog?>(null) }
    var logToDelete by remember { mutableStateOf<FuelLog?>(null) }
    var serviceToDelete by remember { mutableStateOf<ServiceLog?>(null) }
    var languageIsFarsi by remember { mutableStateOf(true) } // Default to Persian as requested

    // Handle back button to return to Fuel (home) if on other screens
    BackHandler(enabled = currentScreen != AppScreen.FUEL) {
        currentScreen = AppScreen.FUEL
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = when (currentScreen) {
                                AppScreen.FUEL -> Color(0xFF005AC1)
                                AppScreen.SERVICES -> Color(0xFF1E3A8A)
                                AppScreen.REPORTS -> Color(0xFF0D9488)
                            },
                            shadowElevation = 4.dp,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when (currentScreen) {
                                        AppScreen.FUEL -> Icons.Default.Info
                                        AppScreen.SERVICES -> Icons.Default.Build
                                        AppScreen.REPORTS -> Icons.Default.Star
                                    },
                                    contentDescription = "Logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val titleText = when (currentScreen) {
                                AppScreen.FUEL -> if (languageIsFarsi) "مدیریت سوخت" else "Fuel Tracker"
                                AppScreen.SERVICES -> if (languageIsFarsi) "سرویس و نگهداری" else "Vehicle Services"
                                AppScreen.REPORTS -> if (languageIsFarsi) "گزارشات و نمودارها" else "Analytics & Reports"
                            }
                            val subText = when (currentScreen) {
                                AppScreen.FUEL -> if (languageIsFarsi) "داده‌های آفلاین درون گوشی" else "Local Native Persistence"
                                AppScreen.SERVICES -> if (languageIsFarsi) "روغن، تعمیرات و بیمه" else "Oil, Repairs & Insurance"
                                AppScreen.REPORTS -> if (languageIsFarsi) "نمودار مصرف و هزینه‌ها" else "Consumption & Cost Breakdown"
                            }
                            Text(
                                text = titleText,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = Color(0xFF0F172A),
                                modifier = Modifier.testTag("app_title")
                            )
                            Text(
                                text = subText,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF334155),
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    // Backup & Restore button
                    IconButton(
                        onClick = { showBackupRestoreDialog = true },
                        modifier = Modifier.testTag("backup_restore_btn")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = if (languageIsFarsi) "پشتیبان‌گیری و بازگردانی" else "Backup & Restore",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Quick language toggle
                    IconButton(
                        onClick = { languageIsFarsi = !languageIsFarsi },
                        modifier = Modifier.testTag("lang_toggle_btn")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (languageIsFarsi) "EN" else "فا",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    // Unit system selector icon menu
                    var showUnitMenu by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { showUnitMenu = true },
                        modifier = Modifier.testTag("unit_menu_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Units",
                            tint = Color(0xFF0F172A)
                        )
                    }

                    DropdownMenu(
                        expanded = showUnitMenu,
                        onDismissRequest = { showUnitMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("L/100 km (Metric)") },
                            onClick = {
                                viewModel.setUnitSystem(UnitSystem.METRIC_L_100KM)
                                showUnitMenu = false
                            },
                            leadingIcon = {
                                if (unitSystem == UnitSystem.METRIC_L_100KM) {
                                    Icon(Icons.Default.Check, "Selected")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("km/L (Metric Alt)") },
                            onClick = {
                                viewModel.setUnitSystem(UnitSystem.METRIC_KM_L)
                                showUnitMenu = false
                            },
                            leadingIcon = {
                                if (unitSystem == UnitSystem.METRIC_KM_L) {
                                    Icon(Icons.Default.Check, "Selected")
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("MPG (Imperial)") },
                            onClick = {
                                viewModel.setUnitSystem(UnitSystem.IMPERIAL_MPG)
                                showUnitMenu = false
                            },
                            leadingIcon = {
                                if (unitSystem == UnitSystem.IMPERIAL_MPG) {
                                    Icon(Icons.Default.Check, "Selected")
                                }
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color(0xFF0F172A),
                    actionIconContentColor = Color(0xFF0F172A)
                )
            )
        },
        bottomBar = {
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF0052B4),
                selectedTextColor = Color(0xFF0052B4),
                indicatorColor = Color(0xFFDBEAFE),
                unselectedIconColor = Color(0xFF334155),
                unselectedTextColor = Color(0xFF334155)
            )
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                // 1. سوخت (Fuel - Main/Home)
                NavigationBarItem(
                    selected = currentScreen == AppScreen.FUEL,
                    onClick = { currentScreen = AppScreen.FUEL },
                    icon = { Icon(Icons.Default.Info, contentDescription = "Fuel") },
                    label = { Text(if (languageIsFarsi) "سوخت" else "Fuel", fontWeight = FontWeight.Bold) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_fuel")
                )

                // 2. سرویس‌ها (Services)
                NavigationBarItem(
                    selected = currentScreen == AppScreen.SERVICES,
                    onClick = { currentScreen = AppScreen.SERVICES },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (serviceStats.alerts.any { it.isOverdue }) {
                                    Badge { Text("!") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Build, contentDescription = "Services")
                        }
                    },
                    label = { Text(if (languageIsFarsi) "سرویس‌ها" else "Services", fontWeight = FontWeight.Bold) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_services")
                )

                // 3. گزارشات (Reports)
                NavigationBarItem(
                    selected = currentScreen == AppScreen.REPORTS,
                    onClick = { currentScreen = AppScreen.REPORTS },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Reports") },
                    label = { Text(if (languageIsFarsi) "گزارشات" else "Reports", fontWeight = FontWeight.Bold) },
                    colors = navItemColors,
                    modifier = Modifier.testTag("nav_reports")
                )
            }
        },
        floatingActionButton = {
            when (currentScreen) {
                AppScreen.FUEL -> {
                    FloatingActionButton(
                        onClick = { showAddFuelDialog = true },
                        containerColor = Color(0xFF005AC1),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("add_log_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Log")
                    }
                }
                AppScreen.SERVICES -> {
                    FloatingActionButton(
                        onClick = { showAddServiceDialog = true },
                        containerColor = Color(0xFF1E3A8A),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .navigationBarsPadding()
                            .testTag("add_service_fab")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Service")
                    }
                }
                AppScreen.REPORTS -> {
                    // No FAB needed on reports
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF1F5F9))
                .drawBehind {
                    // Modern glowing frosted/blurred bubble 1
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x18005AC1), Color(0x00000000)),
                            center = Offset(size.width * 0.15f, size.height * 0.2f),
                            radius = size.width * 0.7f
                        ),
                        center = Offset(size.width * 0.15f, size.height * 0.2f),
                        radius = size.width * 0.7f
                    )
                    // Modern glowing frosted/blurred bubble 2
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0x12005AC1), Color(0x00000000)),
                            center = Offset(size.width * 0.85f, size.height * 0.75f),
                            radius = size.width * 0.8f
                        ),
                        center = Offset(size.width * 0.85f, size.height * 0.75f),
                        radius = size.width * 0.8f
                    )
                }
        ) {
            when (currentScreen) {
                AppScreen.FUEL -> {
                    FuelScreenContent(
                        uiStateLogs = uiStateLogs,
                        stats = stats,
                        unitSystem = unitSystem,
                        currencySymbol = currencySymbol,
                        languageIsFarsi = languageIsFarsi,
                        onCurrencySelected = { viewModel.setCurrencySymbol(it) },
                        onEditClick = { logToEdit = it },
                        onDeleteClick = { logToDelete = it }
                    )
                }
                AppScreen.SERVICES -> {
                    ServicesScreen(
                        services = servicesList,
                        serviceStats = serviceStats,
                        unitSystem = unitSystem,
                        currencySymbol = currencySymbol,
                        languageIsFarsi = languageIsFarsi,
                        onEditService = { serviceToEdit = it },
                        onDeleteService = { serviceToDelete = it },
                        onAddServiceClick = { showAddServiceDialog = true }
                    )
                }
                AppScreen.REPORTS -> {
                    ReportsScreen(
                        reportsData = reportsData,
                        selectedPeriod = selectedPeriod,
                        customDateRange = customDateRange,
                        unitSystem = unitSystem,
                        currencySymbol = currencySymbol,
                        languageIsFarsi = languageIsFarsi,
                        onPeriodSelected = { viewModel.setReportPeriod(it) },
                        onCustomDateRangeSelected = { start, end -> viewModel.setCustomDateRange(start, end) }
                    )
                }
            }
        }
    }

    // Modal forms and dialogs
    if (showAddFuelDialog) {
        val latestOdometer = uiStateLogs.firstOrNull()?.log?.odometer ?: 0.0
        AddLogDialog(
            latestOdometer = latestOdometer,
            unitSystem = unitSystem,
            currencySymbol = currencySymbol,
            languageIsFarsi = languageIsFarsi,
            onDismiss = { showAddFuelDialog = false },
            onSave = { odo, fuel, cost, date, notes ->
                viewModel.insertLog(odo, fuel, cost, date, notes)
                showAddFuelDialog = false
            }
        )
    }

    if (logToEdit != null) {
        AddLogDialog(
            latestOdometer = 0.0,
            unitSystem = unitSystem,
            currencySymbol = currencySymbol,
            languageIsFarsi = languageIsFarsi,
            logToEdit = logToEdit,
            onDismiss = { logToEdit = null },
            onSave = { odo, fuel, cost, date, notes ->
                logToEdit?.let { current ->
                    viewModel.updateLog(
                        current.copy(
                            odometer = odo,
                            fuelAmount = fuel,
                            totalCost = cost,
                            date = date,
                            notes = notes
                        )
                    )
                }
                logToEdit = null
            }
        )
    }

    if (showAddServiceDialog) {
        val latestFuelOdo = uiStateLogs.firstOrNull()?.log?.odometer ?: 0.0
        val latestServiceOdo = servicesList.firstOrNull()?.odometer ?: 0.0
        val latestOdo = maxOf(latestFuelOdo, latestServiceOdo)

        AddServiceDialog(
            latestOdometer = latestOdo,
            unitSystem = unitSystem,
            currencySymbol = currencySymbol,
            languageIsFarsi = languageIsFarsi,
            onDismiss = { showAddServiceDialog = false },
            onSave = { serviceType, date, odo, nextDue, cost, provider, notes ->
                viewModel.insertService(serviceType, date, odo, nextDue, cost, provider, notes)
                showAddServiceDialog = false
            }
        )
    }

    if (serviceToEdit != null) {
        AddServiceDialog(
            latestOdometer = 0.0,
            unitSystem = unitSystem,
            currencySymbol = currencySymbol,
            languageIsFarsi = languageIsFarsi,
            serviceToEdit = serviceToEdit,
            onDismiss = { serviceToEdit = null },
            onSave = { serviceType, date, odo, nextDue, cost, provider, notes ->
                serviceToEdit?.let { current ->
                    viewModel.updateService(
                        current.copy(
                            serviceType = serviceType,
                            date = date,
                            odometer = odo,
                            nextDueOdometer = nextDue,
                            cost = cost,
                            provider = provider,
                            notes = notes
                        )
                    )
                }
                serviceToEdit = null
            }
        )
    }

    if (logToDelete != null) {
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = {
                Text(text = if (languageIsFarsi) "حذف سوخت‌گیری؟" else "Delete Entry?")
            },
            text = {
                Text(text = if (languageIsFarsi) "آیا مطمئن هستید که می‌خواهید این سابقه سوخت‌گیری را حذف کنید؟" else "Are you sure you want to delete this fuel fill-up log?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        logToDelete?.let { viewModel.deleteLog(it.id) }
                        logToDelete = null
                    },
                    modifier = Modifier.testTag("delete_confirm_btn")
                ) {
                    Text(text = if (languageIsFarsi) "بله، حذف شود" else "Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { logToDelete = null }
                ) {
                    Text(text = if (languageIsFarsi) "انصراف" else "Cancel")
                }
            }
        )
    }

    if (serviceToDelete != null) {
        AlertDialog(
            onDismissRequest = { serviceToDelete = null },
            title = {
                Text(text = if (languageIsFarsi) "حذف سابقه سرویس؟" else "Delete Service Entry?")
            },
            text = {
                Text(text = if (languageIsFarsi) "آیا می‌خواهید این رکورد سرویس را از پایگاه داده حذف کنید؟" else "Are you sure you want to delete this service record?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        serviceToDelete?.let { viewModel.deleteService(it.id) }
                        serviceToDelete = null
                    },
                    modifier = Modifier.testTag("delete_service_confirm_btn")
                ) {
                    Text(text = if (languageIsFarsi) "بله، حذف شود" else "Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { serviceToDelete = null }
                ) {
                    Text(text = if (languageIsFarsi) "انصراف" else "Cancel")
                }
            }
        )
    }

    if (showBackupRestoreDialog) {
        BackupRestoreDialog(
            viewModel = viewModel,
            languageIsFarsi = languageIsFarsi,
            onDismiss = { showBackupRestoreDialog = false }
        )
    }
}

@Composable
fun FuelScreenContent(
    uiStateLogs: List<FuelLogUiModel>,
    stats: Statistics,
    unitSystem: UnitSystem,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    onCurrencySelected: (String) -> Unit,
    onEditClick: (FuelLog) -> Unit,
    onDeleteClick: (FuelLog) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Stats Panel Widget
        StatsDashboard(
            stats = stats,
            unitSystem = unitSystem,
            currencySymbol = currencySymbol,
            languageIsFarsi = languageIsFarsi,
            onCurrencySelected = onCurrencySelected
        )

        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (languageIsFarsi) "سوابق سوخت‌گیری" else "Fill-up History",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF0F172A)
            )
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFE2E8F0)
            ) {
                Text(
                    text = "${uiStateLogs.size} ${if (languageIsFarsi) "ثبت شده" else "logs"}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // Entries List
        if (uiStateLogs.isEmpty()) {
            EmptyStateWidget(languageIsFarsi)
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .testTag("logs_list"),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
                items(uiStateLogs, key = { it.log.id }) { uiModel ->
                    FuelLogItemCard(
                        uiModel = uiModel,
                        unitSystem = unitSystem,
                        currencySymbol = currencySymbol,
                        languageIsFarsi = languageIsFarsi,
                        onEditClick = { onEditClick(uiModel.log) },
                        onDeleteClick = { onDeleteClick(uiModel.log) }
                    )
                }
            }
        }
    }
}

@Composable
fun StatsDashboard(
    stats: Statistics,
    unitSystem: UnitSystem,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    onCurrencySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "کیلومتر" else "km"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "مایل" else "mi"
    }

    val fuelUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "لیتر" else "L"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "گالن" else "gal"
    }

    val efficiencyUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM -> if (languageIsFarsi) "لیتر/۱۰۰ک‌م" else "L/100km"
        UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "ک‌م/لیتر" else "km/L"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "مایل/گالن" else "MPG"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFF005AC1), Color(0xFF003780))
                    )
                )
                .drawBehind {
                    // Glass blurred sphere on top-right corners
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        center = Offset(size.width * 1.0f, size.height * 0.0f),
                        radius = size.width * 0.4f
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.04f),
                        center = Offset(size.width * 0.9f, size.height * 0.1f),
                        radius = size.width * 0.6f
                    )
                }
                .padding(20.dp)
        ) {
            // Dashboard Header with Glass Gauge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Frosted circle canvas gauge representation
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val indicatorColor = Color.White
                    val trackColor = Color.White.copy(alpha = 0.2f)
                    val valueFraction = if (stats.averageEfficiency != null) {
                        val eff = stats.averageEfficiency
                        when (unitSystem) {
                            UnitSystem.METRIC_L_100KM -> {
                                (1.0 - (eff / 20.0)).coerceIn(0.1, 0.95)
                            }
                            UnitSystem.METRIC_KM_L -> {
                                (eff / 30.0).coerceIn(0.1, 0.95)
                            }
                            UnitSystem.IMPERIAL_MPG -> {
                                (eff / 50.0).coerceIn(0.1, 0.95)
                            }
                        }
                    } else 0.5

                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawArc(
                            color = trackColor,
                            startAngle = -220f,
                            sweepAngle = 260f,
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                        drawArc(
                            color = indicatorColor,
                            startAngle = -220f,
                            sweepAngle = (260f * valueFraction).toFloat(),
                            useCenter = false,
                            style = Stroke(width = 6.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Fuel Info",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = if (languageIsFarsi) "میانگین" else "Avg",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (languageIsFarsi) "میانگین مصرف دوره" else "Overall Efficiency",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                    Text(
                        text = if (stats.averageEfficiency != null) {
                            String.format(Locale.US, "%.2f %s", stats.averageEfficiency, efficiencyUnit)
                        } else {
                            if (languageIsFarsi) "ثبت ۲ فیش لازم است" else "Need 2 entries"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        fontSize = 26.sp
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = Color.White.copy(alpha = 0.15f)
            )

            // Grid statistics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatSubCard(
                    title = if (languageIsFarsi) "کل هزینه" else "Total Spend",
                    value = String.format(Locale.US, "%,.0f %s", stats.totalCost, currencySymbol),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                StatSubCard(
                    title = if (languageIsFarsi) "مسافت کل" else "Total Distance",
                    value = String.format(Locale.US, "%,.0f %s", stats.totalDistance, distanceUnit),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(12.dp))

                StatSubCard(
                    title = if (languageIsFarsi) "کل سوخت" else "Total Fuel",
                    value = String.format(Locale.US, "%.1f %s", stats.totalFuel, fuelUnit),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Active Currency Glass Picker
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Currency selector",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (languageIsFarsi) "واحد پول فعال:" else "Active Currency:",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                val currencies = listOf("تومان", "﷼", "$", "€")
                Row {
                    currencies.forEach { curr ->
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (currencySymbol == curr) Color.White.copy(alpha = 0.25f)
                                    else Color.Transparent
                                )
                                .clickable { onCurrencySelected(curr) }
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = curr,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatSubCard(
    title: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.16f)
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.92f),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FuelLogItemCard(
    uiModel: FuelLogUiModel,
    unitSystem: UnitSystem,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "کیلومتر" else "km"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "مایل" else "mi"
    }

    val fuelUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "لیتر" else "L"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "گالن" else "gal"
    }

    val efficiencyUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM -> if (languageIsFarsi) "لیتر/۱۰۰ک‌م" else "L/100km"
        UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "ک‌م/لیتر" else "km/L"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "MPG" else "MPG"
    }

    val formattedDate = PersianDateHelper.formatDisplayDate(uiModel.log.date, languageIsFarsi, includeMonthName = true)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("log_item_card_${uiModel.log.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(0xFF0F172A)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header: Date and Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFDBEAFE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date Emblem",
                            tint = Color(0xFF0052B4),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (languageIsFarsi) "جایگاه سوخت" else "Fill-up Station",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569),
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("edit_btn_${uiModel.log.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF0052B4),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("delete_btn_${uiModel.log.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = Color(0xFFDC2626),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Body: Core fill up info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Info block 1
                Column {
                    Text(
                        text = if (languageIsFarsi) "کیلومتر شمار" else "Odometer",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%,.0f %s", uiModel.log.odometer, distanceUnit),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        fontSize = 15.sp
                    )
                }

                // Info block 2
                Column {
                    Text(
                        text = if (languageIsFarsi) "حجم سوخت" else "Refilled",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%.1f %s", uiModel.log.fuelAmount, fuelUnit),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        fontSize = 15.sp
                    )
                }

                // Info block 3
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (languageIsFarsi) "هزینه کل" else "Total Cost",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%,.0f %s", uiModel.log.totalCost, currencySymbol),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0052B4),
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Calculated interval stats (Distance since previous & fuel consumption)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF1F5F9))
                    .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Interval travel calculation
                Column {
                    val distanceText = if (uiModel.distanceTraveled != null) {
                        String.format(Locale.US, "+%,.0f %s", uiModel.distanceTraveled, distanceUnit)
                    } else "---"

                    Text(
                        text = if (languageIsFarsi) "مسافت طی شده" else "Interval Distance",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = distanceText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        fontSize = 14.sp
                    )
                }

                // Interval efficiency calculation
                Column(horizontalAlignment = Alignment.End) {
                    val efficiencyText = if (uiModel.efficiency != null) {
                        String.format(Locale.US, "%.2f %s", uiModel.efficiency, efficiencyUnit)
                    } else {
                        if (languageIsFarsi) "اولین ثبت شده" else "Initial Fill-up"
                    }

                    Text(
                        text = if (languageIsFarsi) "مصرف این دوره" else "Interval Efficiency",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = efficiencyText,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (uiModel.efficiency != null) Color(0xFF0052B4) else Color(0xFF334155),
                        fontSize = 14.sp
                    )
                }
            }

            // Notes if available
            if (uiModel.log.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Notes",
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = uiModel.log.notes,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF1E293B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun AddLogDialog(
    latestOdometer: Double,
    unitSystem: UnitSystem,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    logToEdit: FuelLog? = null,
    onDismiss: () -> Unit,
    onSave: (odometer: Double, fuelAmount: Double, totalCost: Double, date: Long, notes: String) -> Unit
) {
    var odoInput by remember {
        mutableStateOf(
            if (logToEdit != null) {
                if (logToEdit.odometer % 1.0 == 0.0) logToEdit.odometer.toLong().toString() else logToEdit.odometer.toString()
            } else ""
        )
    }
    var fuelInput by remember {
        mutableStateOf(
            if (logToEdit != null) {
                if (logToEdit.fuelAmount % 1.0 == 0.0) logToEdit.fuelAmount.toLong().toString() else logToEdit.fuelAmount.toString()
            } else ""
        )
    }
    var costInput by remember {
        mutableStateOf(
            if (logToEdit != null) {
                if (logToEdit.totalCost % 1.0 == 0.0) logToEdit.totalCost.toLong().toString() else logToEdit.totalCost.toString()
            } else ""
        )
    }
    var notesInput by remember { mutableStateOf(logToEdit?.notes ?: "") }
    var dateInput by remember { mutableStateOf(logToEdit?.date ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var odoError by remember { mutableStateOf<String?>(null) }
    var fuelError by remember { mutableStateOf<String?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }

    val distanceUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "کیلومتر" else "km"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "مایل" else "mi"
    }

    val fuelUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "لیتر" else "L"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "گالن" else "gal"
    }

    if (showDatePicker) {
        PersianDatePickerDialog(
            initialDateMillis = dateInput,
            languageIsFarsi = languageIsFarsi,
            onDismiss = { showDatePicker = false },
            onDateSelected = {
                dateInput = it
                showDatePicker = false
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp)
                .testTag("add_log_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Dialog Title
                Text(
                    text = if (logToEdit != null) {
                        if (languageIsFarsi) "ویرایش سابقه سوخت‌گیری" else "Edit Fuel Entry"
                    } else {
                        if (languageIsFarsi) "ثبت سوخت‌گیری جدید" else "New Fuel Entry"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Date Selector Card (Solar Hijri)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showDatePicker = true }
                        .testTag("pick_fuel_date_btn")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (languageIsFarsi) "تاریخ سوخت‌گیری (هجری شمسی):" else "Fill-up Date:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = PersianDateHelper.formatDisplayDate(dateInput, languageIsFarsi, true),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        FilledTonalIconButton(
                            onClick = { showDatePicker = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Change Date",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Showing latest registered odometer
                if (latestOdometer > 0.0 && logToEdit == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                            .padding(all = 10.dp)
                    ) {
                        Text(
                            text = if (languageIsFarsi)
                                "آخرین کیلومتر سوخت‌گیری شده: %,.0f %s".format(latestOdometer, distanceUnit)
                            else
                                "Latest recorded odometer: %,.0f %s".format(latestOdometer, distanceUnit),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Odometer Input
                OutlinedTextField(
                    value = odoInput,
                    onValueChange = {
                        odoInput = it
                        odoError = null
                        if (it.isNotBlank()) {
                            val value = it.toDoubleOrNull()
                            if (value == null) {
                                odoError = if (languageIsFarsi) "مقدار باید عددی باشد" else "Value must be numeric"
                            } else if (logToEdit == null && value <= latestOdometer && latestOdometer > 0) {
                                odoError = if (languageIsFarsi)
                                    "کیلومتر باید بیشتر از آخرین ثبت ($latestOdometer) باشد"
                                else
                                    "Odometer must be greater than previous ($latestOdometer)"
                            } else if (value < 0) {
                                odoError = if (languageIsFarsi) "مقدار نمی‌تواند منفی باشد" else "Cannot be negative"
                            }
                        }
                    },
                    label = {
                        Text(if (languageIsFarsi) "کیلومتر شمار ($distanceUnit) *" else "Odometer ($distanceUnit) *")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = odoError != null,
                    supportingText = { odoError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("odometer_input")
                )

                // Fuel Filled Input
                OutlinedTextField(
                    value = fuelInput,
                    onValueChange = {
                        fuelInput = it
                        fuelError = null
                        if (it.isNotBlank()) {
                            val value = it.toDoubleOrNull()
                            if (value == null) {
                                fuelError = if (languageIsFarsi) "مقدار باید عددی باشد" else "Value must be numeric"
                            } else if (value <= 0) {
                                fuelError = if (languageIsFarsi) "مقدار باید بیشتر از صفر باشد" else "Must be greater than zero"
                            }
                        }
                    },
                    label = {
                        Text(if (languageIsFarsi) "مقدار سوخت ($fuelUnit) *" else "Fuel Refilled ($fuelUnit) *")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = fuelError != null,
                    supportingText = { fuelError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("fuel_input")
                )

                // Total Cost Input
                OutlinedTextField(
                    value = costInput,
                    onValueChange = {
                        costInput = it
                        costError = null
                        if (it.isNotBlank()) {
                            val value = it.toDoubleOrNull()
                            if (value == null) {
                                costError = if (languageIsFarsi) "مقدار باید عددی باشد" else "Value must be numeric"
                            } else if (value <= 0) {
                                costError = if (languageIsFarsi) "هزینه باید بیشتر از صفر باشد" else "Must be greater than zero"
                            }
                        }
                    },
                    label = {
                        Text(if (languageIsFarsi) "هزینه کل ($currencySymbol) *" else "Total Cost ($currencySymbol) *")
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    isError = costError != null,
                    supportingText = { costError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("cost_input")
                )

                // Notes Input
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text(if (languageIsFarsi) "یادداشت‌ها (اختیاری)" else "Notes (Optional)") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                        .testTag("notes_input")
                )

                // Actions buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (languageIsFarsi) "انصراف" else "Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            val odo = odoInput.toDoubleOrNull()
                            val fuel = fuelInput.toDoubleOrNull()
                            val cost = costInput.toDoubleOrNull()

                            if (odo == null || (logToEdit == null && latestOdometer > 0 && odo <= latestOdometer) || odo < 0) {
                                odoError = if (languageIsFarsi) "اطلاعات نامعتبر است" else "Invalid odometer value"
                            }
                            if (fuel == null || fuel <= 0) {
                                fuelError = if (languageIsFarsi) "واحد نامعتبر است" else "Invalid fuel value"
                            }
                            if (cost == null || cost <= 0) {
                                costError = if (languageIsFarsi) "هزینه نامعتبر است" else "Invalid cost value"
                            }

                            if (odoError == null && fuelError == null && costError == null && odo != null && fuel != null && cost != null) {
                                onSave(odo, fuel, cost, dateInput, notesInput)
                            }
                        },
                        modifier = Modifier.testTag("dialog_save_btn")
                    ) {
                        Text(
                            if (logToEdit != null) {
                                if (languageIsFarsi) "ذخیره تغییرات" else "Save Changes"
                            } else {
                                if (languageIsFarsi) "ذخیره" else "Save"
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateWidget(languageIsFarsi: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "No logs yet",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (languageIsFarsi) "هیچ سابقه‌ای ثبت نشده است" else "Your Tank is Empty!",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF0F172A)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (languageIsFarsi)
                "روی دکمه ثبت سوخت (+) در پایین کلیک کنید تا اولین سوخت‌گیری خود را وارد کنید."
            else
                "Tap the + button below to add your first gas fill-up. The app will calculate fuel economy dynamically once you register 2 logs.",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            color = Color(0xFF334155),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
