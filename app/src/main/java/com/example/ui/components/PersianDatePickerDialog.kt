package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.util.PersianDateHelper
import java.util.Calendar

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersianDatePickerDialog(
    initialDateMillis: Long,
    languageIsFarsi: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (selectedMillis: Long) -> Unit
) {
    val initialPDate = remember(initialDateMillis) {
        PersianDateHelper.millisToPersian(initialDateMillis)
    }

    var selectedYear by remember { mutableIntStateOf(initialPDate.year) }
    var selectedMonth by remember { mutableIntStateOf(initialPDate.month) } // 1..12
    var selectedDay by remember { mutableIntStateOf(initialPDate.day) } // 1..31

    // Ensure day doesn't exceed days in newly selected month/year
    val maxDaysInCurrentMonth = remember(selectedYear, selectedMonth) {
        PersianDateHelper.getDaysInPersianMonth(selectedYear, selectedMonth)
    }
    LaunchedEffect(maxDaysInCurrentMonth) {
        if (selectedDay > maxDaysInCurrentMonth) {
            selectedDay = maxDaysInCurrentMonth
        }
    }

    val todayPDate = remember {
        PersianDateHelper.millisToPersian(System.currentTimeMillis())
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(4.dp)
                .testTag("persian_date_picker_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Calendar Icon and Formatted Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (languageIsFarsi) "انتخاب تاریخ شمسی" else "Select Solar Date",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val monthName = PersianDateHelper.PERSIAN_MONTH_NAMES.getOrElse(selectedMonth - 1) { "" }
                        Text(
                            text = if (languageIsFarsi)
                                "$selectedDay $monthName $selectedYear"
                            else
                                "$selectedYear/$selectedMonth/$selectedDay",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick Buttons (Today / Yesterday)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedYear == todayPDate.year && selectedMonth == todayPDate.month && selectedDay == todayPDate.day,
                        onClick = {
                            selectedYear = todayPDate.year
                            selectedMonth = todayPDate.month
                            selectedDay = todayPDate.day
                        },
                        label = { Text(if (languageIsFarsi) "امروز" else "Today") },
                        modifier = Modifier.weight(1f)
                    )

                    val yesterdayPDate = remember {
                        PersianDateHelper.millisToPersian(System.currentTimeMillis() - 86400000L)
                    }
                    FilterChip(
                        selected = selectedYear == yesterdayPDate.year && selectedMonth == yesterdayPDate.month && selectedDay == yesterdayPDate.day,
                        onClick = {
                            selectedYear = yesterdayPDate.year
                            selectedMonth = yesterdayPDate.month
                            selectedDay = yesterdayPDate.day
                        },
                        label = { Text(if (languageIsFarsi) "دیروز" else "Yesterday") },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Year Selector with Stepper and Display
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { selectedYear-- },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous Year",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (languageIsFarsi) "سال" else "Year",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                                fontSize = 10.sp
                            )
                            Text(
                                text = "$selectedYear",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        IconButton(
                            onClick = { selectedYear++ },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next Year",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Month Selector Grid (12 Months)
                Text(
                    text = if (languageIsFarsi) "ماه:" else "Month:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = 4
                ) {
                    PersianDateHelper.PERSIAN_MONTH_NAMES.forEachIndexed { index, mName ->
                        val monthNum = index + 1
                        val isSelected = selectedMonth == monthNum

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = if (isSelected) null else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedMonth = monthNum }
                        ) {
                            Text(
                                text = if (languageIsFarsi) mName else PersianDateHelper.PERSIAN_MONTH_NAMES_EN[index],
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Day Selector Grid
                Text(
                    text = if (languageIsFarsi) "روز:" else "Day:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    maxItemsInEachRow = 7
                ) {
                    for (dayNum in 1..maxDaysInCurrentMonth) {
                        val isSelected = selectedDay == dayNum
                        val isToday = (selectedYear == todayPDate.year && selectedMonth == todayPDate.month && dayNum == todayPDate.day)

                        Surface(
                            shape = CircleShape,
                            color = when {
                                isSelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> Color.Transparent
                            },
                            border = when {
                                isSelected -> null
                                isToday -> BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                                else -> BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .clickable { selectedDay = dayNum }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$dayNum",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> MaterialTheme.colorScheme.primary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Actions (Cancel / Confirm)
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
                            val computedMillis = PersianDateHelper.persianToMillis(selectedYear, selectedMonth, selectedDay)
                            onDateSelected(computedMillis)
                        },
                        modifier = Modifier.testTag("confirm_persian_date_btn")
                    ) {
                        Text(if (languageIsFarsi) "تأیید تاریخ" else "Confirm Date")
                    }
                }
            }
        }
    }
}
