package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.util.PersianDateHelper

@Composable
fun CustomDateRangeDialog(
    initialStartDate: Long?,
    initialEndDate: Long?,
    languageIsFarsi: Boolean,
    onDismiss: () -> Unit,
    onApply: (startMillis: Long, endMillis: Long) -> Unit
) {
    val now = System.currentTimeMillis()

    // Default to last 30 days if not set
    var startDateMillis by remember {
        mutableStateOf(initialStartDate ?: (now - 30L * 24 * 3600 * 1000))
    }
    var endDateMillis by remember {
        mutableStateOf(initialEndDate ?: now)
    }

    // "START" or "END" or null
    var pickingTarget by remember { mutableStateOf<String?>(null) }

    fun formatDate(millis: Long): String {
        return PersianDateHelper.formatDisplayDate(millis, languageIsFarsi, includeMonthName = true)
    }

    if (pickingTarget != null) {
        val initialDate = if (pickingTarget == "START") startDateMillis else endDateMillis
        PersianDatePickerDialog(
            initialDateMillis = initialDate,
            languageIsFarsi = languageIsFarsi,
            onDismiss = { pickingTarget = null },
            onDateSelected = { selectedMillis ->
                if (pickingTarget == "START") {
                    startDateMillis = selectedMillis
                    if (startDateMillis > endDateMillis) {
                        endDateMillis = startDateMillis
                    }
                } else {
                    endDateMillis = selectedMillis
                    if (endDateMillis < startDateMillis) {
                        startDateMillis = endDateMillis
                    }
                }
                pickingTarget = null
            }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(12.dp)
                .testTag("custom_date_range_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = if (languageIsFarsi) "انتخاب بازه زمانی گزارش (شمسی)" else "Select Custom Date Range",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Presets
                Text(
                    text = if (languageIsFarsi) "میانبرهای سریع:" else "Quick Presets:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val presets = listOf(
                        Pair(if (languageIsFarsi) "۳۰ روز اخیر" else "Last 30 Days", 30L),
                        Pair(if (languageIsFarsi) "۶۰ روز اخیر" else "Last 60 Days", 60L),
                        Pair(if (languageIsFarsi) "۹۰ روز اخیر" else "Last 90 Days", 90L),
                        Pair(if (languageIsFarsi) "۶ ماه اخیر" else "Last 6 Months", 180L),
                        Pair(if (languageIsFarsi) "امسال" else "This Year", 365L)
                    )

                    items(presets) { (label, days) ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    endDateMillis = System.currentTimeMillis()
                                    startDateMillis = endDateMillis - (days * 24 * 3600 * 1000)
                                }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Start Date Card Picker
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { pickingTarget = "START" }
                        .testTag("pick_start_date_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (languageIsFarsi) "از تاریخ (شروع)" else "Start Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatDate(startDateMillis),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pick start date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // End Date Card Picker
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { pickingTarget = "END" }
                        .testTag("pick_end_date_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (languageIsFarsi) "تا تاریخ (پایان)" else "End Date",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = formatDate(endDateMillis),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Pick end date",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
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
                            val start = minOf(startDateMillis, endDateMillis)
                            val end = maxOf(startDateMillis, endDateMillis)
                            onApply(start, end)
                        },
                        modifier = Modifier.testTag("apply_date_range_btn")
                    ) {
                        Text(if (languageIsFarsi) "اعمال بازه" else "Apply Filter")
                    }
                }
            }
        }
    }
}
