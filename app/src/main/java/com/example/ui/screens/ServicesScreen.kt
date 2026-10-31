package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ServiceLog
import com.example.ui.ServiceAlert
import com.example.ui.ServiceStatistics
import com.example.ui.UnitSystem
import com.example.ui.components.SERVICE_CATEGORIES
import com.example.util.PersianDateHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ServicesScreen(
    services: List<ServiceLog>,
    serviceStats: ServiceStatistics,
    unitSystem: UnitSystem,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    onDeleteService: (ServiceLog) -> Unit,
    onEditService: (ServiceLog) -> Unit,
    onAddServiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val distanceUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "کیلومتر" else "km"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "مایل" else "mi"
    }

    val filteredServices = if (selectedFilter == "ALL") {
        services
    } else {
        services.filter { it.serviceType == selectedFilter }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("services_list"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Summary Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("services_summary_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (languageIsFarsi) "کل هزینه‌های سرویس و تعمیرات" else "Total Maintenance Spend",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White.copy(alpha = 0.92f)
                                )
                                Text(
                                    text = String.format(Locale.US, "%,.0f %s", serviceStats.totalServiceCost, currencySymbol),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                            Surface(
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Build,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (languageIsFarsi) "دفعات سرویس" else "Services Done",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "${serviceStats.serviceCount} ${if (languageIsFarsi) "مورد" else "items"}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }

                            val topCategory = serviceStats.costByType.maxByOrNull { it.value }
                            val topCatName = if (topCategory != null) {
                                val item = SERVICE_CATEGORIES.firstOrNull { it.key == topCategory.key }
                                if (languageIsFarsi) (item?.titleFa ?: topCategory.key) else (item?.titleEn ?: topCategory.key)
                            } else "---"

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.18f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = if (languageIsFarsi) "بیشترین بخش هزینه" else "Highest Category",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color.White.copy(alpha = 0.92f),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = topCatName,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Active Alerts / Reminders
        if (serviceStats.alerts.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (languageIsFarsi) "یادآوری‌های موعد سرویس" else "Upcoming Maintenance Alerts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A)
                    )
                    serviceStats.alerts.take(2).forEach { alert ->
                        AlertCard(alert = alert, unit = distanceUnit, languageIsFarsi = languageIsFarsi)
                    }
                }
            }
        }

        // Filter Chips
        item {
            val chipColors = FilterChipDefaults.filterChipColors(
                containerColor = Color.White,
                labelColor = Color(0xFF1E293B),
                iconColor = Color(0xFF334155),
                selectedContainerColor = Color(0xFF0052B4),
                selectedLabelColor = Color.White,
                selectedLeadingIconColor = Color.White
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                item {
                    val isAllSelected = selectedFilter == "ALL"
                    FilterChip(
                        selected = isAllSelected,
                        onClick = { selectedFilter = "ALL" },
                        colors = chipColors,
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isAllSelected,
                            borderColor = Color(0xFF94A3B8),
                            selectedBorderColor = Color(0xFF0052B4),
                            borderWidth = 1.dp
                        ),
                        label = {
                            Text(
                                text = if (languageIsFarsi) "همه (${services.size})" else "All (${services.size})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    )
                }
                items(SERVICE_CATEGORIES) { cat ->
                    val count = services.count { it.serviceType == cat.key }
                    if (count > 0 || selectedFilter == cat.key) {
                        val isCatSelected = selectedFilter == cat.key
                        FilterChip(
                            selected = isCatSelected,
                            onClick = { selectedFilter = cat.key },
                            colors = chipColors,
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isCatSelected,
                                borderColor = Color(0xFF94A3B8),
                                selectedBorderColor = Color(0xFF0052B4),
                                borderWidth = 1.dp
                            ),
                            leadingIcon = {
                                Icon(
                                    imageVector = cat.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = if (languageIsFarsi) "${cat.titleFa} ($count)" else "${cat.titleEn} ($count)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }
            }
        }

        // Services List or Empty State
        if (filteredServices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF0F172A)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDBEAFE),
                            modifier = Modifier.size(64.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Build,
                                    contentDescription = null,
                                    tint = Color(0xFF0052B4),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (languageIsFarsi) "هنوز سرویسی ثبت نشده است" else "No Services Recorded",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (languageIsFarsi)
                                "سرویس‌های تعویض روغن، تعمیرات، بیمه و سایر هزینه‌های خودرو را ثبت کنید تا تاریخچه و هزینه‌ها دقیق ثبت شود."
                            else
                                "Track your oil changes, maintenance, repairs, and insurance to monitor your car's true running costs.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155),
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = onAddServiceClick,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (languageIsFarsi) "ثبت اولین سرویس" else "Add First Service", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        } else {
            items(filteredServices, key = { it.id }) { service ->
                ServiceItemCard(
                    service = service,
                    unit = distanceUnit,
                    currencySymbol = currencySymbol,
                    languageIsFarsi = languageIsFarsi,
                    onEdit = { onEditService(service) },
                    onDelete = { onDeleteService(service) }
                )
            }
        }
    }
}

@Composable
fun AlertCard(
    alert: ServiceAlert,
    unit: String,
    languageIsFarsi: Boolean
) {
    val category = SERVICE_CATEGORIES.firstOrNull { it.key == alert.serviceType }
    val catTitle = if (languageIsFarsi) (category?.titleFa ?: alert.serviceType) else (category?.titleEn ?: alert.serviceType)

    val bgColor = if (alert.isOverdue) Color(0xFFFEE2E2) else Color(0xFFFEF3C7)
    val contentColor = if (alert.isOverdue) Color(0xFFB91C1C) else Color(0xFFB45309)
    val icon = if (alert.isOverdue) Icons.Default.Warning else Icons.Default.Notifications

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        border = BorderStroke(1.dp, contentColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = contentColor.copy(alpha = 0.16f),
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (alert.isOverdue) {
                        if (languageIsFarsi) "موعد $catTitle فرارسیده یا گذشته است!" else "$catTitle is overdue!"
                    } else {
                        if (languageIsFarsi) "یادآوری $catTitle" else "$catTitle reminder"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Text(
                    text = if (alert.isOverdue) {
                        if (languageIsFarsi) "کیلومتر سررسید: %,.0f $unit (منقضی شده)".format(alert.nextDueOdometer)
                        else "Due at %,.0f $unit (Overdue)".format(alert.nextDueOdometer)
                    } else {
                        if (languageIsFarsi) "حدود %,.0f $unit دیگر (در %,.0f)".format(alert.kmRemaining, alert.nextDueOdometer)
                        else "In %,.0f $unit (at %,.0f)".format(alert.kmRemaining, alert.nextDueOdometer)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF1E293B)
                )
            }
        }
    }
}

@Composable
fun ServiceItemCard(
    service: ServiceLog,
    unit: String,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val category = SERVICE_CATEGORIES.firstOrNull { it.key == service.serviceType }
    val title = if (languageIsFarsi) (category?.titleFa ?: service.serviceType) else (category?.titleEn ?: service.serviceType)
    val icon = category?.icon ?: Icons.Default.Build

    val categoryColor = when (service.serviceType) {
        "OIL" -> Color(0xFFB45309)        // Rich Dark Amber
        "REPAIR" -> Color(0xFFB91C1C)     // Deep Crimson
        "INSURANCE" -> Color(0xFF1D4ED8)  // Deep Royal Blue
        "TIRE" -> Color(0xFF047857)       // Deep Emerald
        "INSPECTION" -> Color(0xFF6D28D9) // Deep Violet
        "WASH" -> Color(0xFF0369A1)       // Deep Ocean Blue
        else -> Color(0xFF334155)         // Deep Slate
    }

    val dateText = PersianDateHelper.formatDisplayDate(service.date, languageIsFarsi, true)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("service_card_${service.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
            contentColor = Color(0xFF0F172A)
        ),
        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = categoryColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = icon, contentDescription = null, tint = categoryColor, modifier = Modifier.size(22.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = dateText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155),
                            fontSize = 12.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(40.dp).testTag("edit_service_btn_${service.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF0052B4),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(40.dp).testTag("delete_service_btn_${service.id}")
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

            Spacer(modifier = Modifier.height(12.dp))

            // Body Metrics
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (languageIsFarsi) "کیلومتر انجام" else "Odometer",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%,.0f %s", service.odometer, unit),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F172A),
                        fontSize = 15.sp
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (languageIsFarsi) "هزینه پرداختی" else "Cost",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF334155),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = String.format(Locale.US, "%,.0f %s", service.cost, currencySymbol),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = categoryColor,
                        fontSize = 15.sp
                    )
                }
            }

            // Next Due and Provider details
            if (service.nextDueOdometer != null && service.nextDueOdometer > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF0052B4),
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (languageIsFarsi)
                                "موعد سرویس بعدی: %,.0f %s".format(service.nextDueOdometer, unit)
                            else
                                "Next due at %,.0f %s".format(service.nextDueOdometer, unit),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E3A8A),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            if (service.provider.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = service.provider,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1E293B)
                    )
                }
            }

            if (service.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFF8FAFC))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF334155),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = service.notes,
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
