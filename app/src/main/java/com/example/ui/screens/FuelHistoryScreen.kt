package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.FuelLogItemCard
import com.example.data.FuelLog
import com.example.ui.FuelLogUiModel
import com.example.ui.UnitSystem
import com.example.ui.components.CustomDateRangeDialog
import com.example.util.PersianDateHelper
import java.util.Locale

enum class HistoryFilterPeriod {
    ALL,
    ONE_MONTH,
    THREE_MONTHS,
    SIX_MONTHS,
    ONE_YEAR,
    CUSTOM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelHistoryScreen(
    allLogs: List<FuelLogUiModel>,
    unitSystem: UnitSystem,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    onBackClick: () -> Unit,
    onEditClick: (FuelLog) -> Unit,
    onDeleteClick: (FuelLog) -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBackClick)

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(HistoryFilterPeriod.ALL) }
    var customStartDate by remember { mutableStateOf<Long?>(null) }
    var customEndDate by remember { mutableStateOf<Long?>(null) }
    var showCustomDateDialog by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()

    // Filter by period and search query
    val filteredLogs = remember(allLogs, searchQuery, selectedFilter, customStartDate, customEndDate) {
        allLogs.filter { uiModel ->
            // 1. Period filter
            val matchesPeriod = when (selectedFilter) {
                HistoryFilterPeriod.ALL -> true
                HistoryFilterPeriod.ONE_MONTH -> uiModel.log.date >= (now - 30L * 24 * 3600 * 1000)
                HistoryFilterPeriod.THREE_MONTHS -> uiModel.log.date >= (now - 90L * 24 * 3600 * 1000)
                HistoryFilterPeriod.SIX_MONTHS -> uiModel.log.date >= (now - 180L * 24 * 3600 * 1000)
                HistoryFilterPeriod.ONE_YEAR -> uiModel.log.date >= (now - 365L * 24 * 3600 * 1000)
                HistoryFilterPeriod.CUSTOM -> {
                    val start = customStartDate ?: 0L
                    val end = if (customEndDate != null) customEndDate!! + (24L * 3600 * 1000 - 1) else Long.MAX_VALUE
                    uiModel.log.date in start..end
                }
            }

            // 2. Search query filter (search in notes, odometer string, cost, or date)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                val q = searchQuery.trim().lowercase(Locale.ROOT)
                val odoStr = uiModel.log.odometer.toInt().toString()
                val notes = uiModel.log.notes.lowercase(Locale.ROOT)
                val costStr = uiModel.log.totalCost.toInt().toString()
                notes.contains(q) || odoStr.contains(q) || costStr.contains(q)
            }

            matchesPeriod && matchesSearch
        }
    }

    // Quick stats on current filtered list
    val totalFilteredCost = remember(filteredLogs) { filteredLogs.sumOf { it.log.totalCost } }
    val totalFilteredFuel = remember(filteredLogs) { filteredLogs.sumOf { it.log.fuelAmount } }

    val fuelUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "لیتر" else "L"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "گالن" else "gal"
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (languageIsFarsi) "آرشیو کامل سوخت‌گیری" else "Complete Fill-up History",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (languageIsFarsi) "${filteredLogs.size} مورد از ${allLogs.size} فیش" else "${filteredLogs.size} of ${allLogs.size} logs",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 11.sp
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("history_back_btn")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (searchQuery.isNotEmpty() || selectedFilter != HistoryFilterPeriod.ALL) {
                        TextButton(
                            onClick = {
                                searchQuery = ""
                                selectedFilter = HistoryFilterPeriod.ALL
                                customStartDate = null
                                customEndDate = null
                            }
                        ) {
                            Text(
                                text = if (languageIsFarsi) "حذف فیلترها" else "Reset",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. Search Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (languageIsFarsi) "جستجو در یادداشت یا کیلومتر..." else "Search notes or odometer...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("history_search_input")
                    )
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = { searchQuery = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Filter Period Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    Pair(HistoryFilterPeriod.ALL, if (languageIsFarsi) "همه (${allLogs.size})" else "All (${allLogs.size})"),
                    Pair(HistoryFilterPeriod.ONE_MONTH, if (languageIsFarsi) "۱ ماه اخیر" else "1 Month"),
                    Pair(HistoryFilterPeriod.THREE_MONTHS, if (languageIsFarsi) "۳ ماه اخیر" else "3 Months"),
                    Pair(HistoryFilterPeriod.SIX_MONTHS, if (languageIsFarsi) "۶ ماه اخیر" else "6 Months"),
                    Pair(HistoryFilterPeriod.ONE_YEAR, if (languageIsFarsi) "۱ سال اخیر" else "1 Year"),
                    Pair(
                        HistoryFilterPeriod.CUSTOM,
                        if (selectedFilter == HistoryFilterPeriod.CUSTOM && customStartDate != null && customEndDate != null) {
                            val startStr = PersianDateHelper.formatDisplayDate(customStartDate!!, languageIsFarsi, includeMonthName = false)
                            val endStr = PersianDateHelper.formatDisplayDate(customEndDate!!, languageIsFarsi, includeMonthName = false)
                            "$startStr - $endStr"
                        } else {
                            if (languageIsFarsi) "بازه دلخواه 📅" else "Custom Range 📅"
                        }
                    )
                )

                items(filters) { (period, label) ->
                    val isSelected = selectedFilter == period
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (period == HistoryFilterPeriod.CUSTOM) {
                                showCustomDateDialog = true
                            } else {
                                selectedFilter = period
                            }
                        },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            containerColor = MaterialTheme.colorScheme.surface,
                            labelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.testTag("history_filter_chip_${period.name}")
                    )
                }
            }

            // 3. Quick Summary Pill
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageIsFarsi) "خلاصه بازه انتخابی:" else "Filtered Summary:",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = "${String.format(Locale.US, "%,.0f %s", totalFilteredCost, currencySymbol)} | ${String.format(Locale.US, "%.1f %s", totalFilteredFuel, fuelUnit)}",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // 4. Fill-up History Cards List
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (languageIsFarsi) "هیچ رکوردی با این مشخصات یافت نشد" else "No matching fuel records found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (languageIsFarsi) "می‌توانید عبارت جستجو را پاک کنید یا بازه زمانی را تغییر دهید." else "Try clearing your search query or expanding the date filter.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                searchQuery = ""
                                selectedFilter = HistoryFilterPeriod.ALL
                                customStartDate = null
                                customEndDate = null
                            }
                        ) {
                            Text(if (languageIsFarsi) "نمایش تمام سوابق" else "Show All Logs")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("full_history_logs_list"),
                    contentPadding = PaddingValues(top = 4.dp, bottom = 32.dp)
                ) {
                    items(filteredLogs, key = { it.log.id }) { uiModel ->
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

    if (showCustomDateDialog) {
        CustomDateRangeDialog(
            initialStartDate = customStartDate,
            initialEndDate = customEndDate,
            languageIsFarsi = languageIsFarsi,
            onDismiss = { showCustomDateDialog = false },
            onApply = { start, end ->
                customStartDate = start
                customEndDate = end
                selectedFilter = HistoryFilterPeriod.CUSTOM
                showCustomDateDialog = false
            }
        )
    }
}
