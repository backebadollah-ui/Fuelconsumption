package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ServiceLog
import com.example.ui.UnitSystem
import com.example.util.PersianDateHelper

data class ServiceCategoryItem(
    val key: String,
    val titleFa: String,
    val titleEn: String,
    val icon: ImageVector,
    val defaultIntervalKm: Double? = null
)

val SERVICE_CATEGORIES = listOf(
    ServiceCategoryItem("OIL", "روغن و فیلترها", "Oil & Filters", Icons.Default.Build, 5000.0),
    ServiceCategoryItem("REPAIR", "تعمیرات و قطعات", "Repairs", Icons.Default.Settings, null),
    ServiceCategoryItem("INSURANCE", "بیمه خودرو", "Insurance", Icons.Default.CheckCircle, null),
    ServiceCategoryItem("TIRE", "لاستیک و باد", "Tires", Icons.Default.Refresh, 20000.0),
    ServiceCategoryItem("INSPECTION", "معاینه فنی و دوره‌ای", "Inspection", Icons.Default.Info, 10000.0),
    ServiceCategoryItem("WASH", "کارواش و نظافت", "Car Wash", Icons.Default.Star, null),
    ServiceCategoryItem("OTHER", "سایر هزینه‌ها", "Other", Icons.Default.MoreVert, null)
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddServiceDialog(
    latestOdometer: Double,
    unitSystem: UnitSystem,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    serviceToEdit: ServiceLog? = null,
    onDismiss: () -> Unit,
    onSave: (serviceType: String, date: Long, odometer: Double, nextDueOdometer: Double?, cost: Double, provider: String, notes: String) -> Unit
) {
    var selectedType by remember { mutableStateOf(serviceToEdit?.serviceType ?: "OIL") }
    var odoInput by remember {
        mutableStateOf(
            if (serviceToEdit != null) {
                if (serviceToEdit.odometer % 1.0 == 0.0) serviceToEdit.odometer.toLong().toString() else serviceToEdit.odometer.toString()
            } else if (latestOdometer > 0) {
                latestOdometer.toLong().toString()
            } else ""
        )
    }
    var nextDueInput by remember {
        val initialNextDue = if (serviceToEdit != null) {
            serviceToEdit.nextDueOdometer?.let {
                if (it % 1.0 == 0.0) it.toLong().toString() else it.toString()
            } ?: ""
        } else {
            val defaultInterval = SERVICE_CATEGORIES.firstOrNull { it.key == "OIL" }?.defaultIntervalKm ?: 5000.0
            if (latestOdometer > 0) (latestOdometer + defaultInterval).toLong().toString() else ""
        }
        mutableStateOf(initialNextDue)
    }
    var costInput by remember {
        mutableStateOf(
            if (serviceToEdit != null) {
                if (serviceToEdit.cost % 1.0 == 0.0) serviceToEdit.cost.toLong().toString() else serviceToEdit.cost.toString()
            } else ""
        )
    }
    var providerInput by remember { mutableStateOf(serviceToEdit?.provider ?: "") }
    var notesInput by remember { mutableStateOf(serviceToEdit?.notes ?: "") }
    var dateInput by remember { mutableStateOf(serviceToEdit?.date ?: System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var odoError by remember { mutableStateOf<String?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }

    val distanceUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "کیلومتر" else "km"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "مایل" else "mi"
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
                .padding(8.dp)
                .testTag("add_service_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF005AC1),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (serviceToEdit != null) Icons.Default.Edit else Icons.Default.Build,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (serviceToEdit != null) {
                            if (languageIsFarsi) "ویرایش سابقه سرویس" else "Edit Vehicle Service"
                        } else {
                            if (languageIsFarsi) "ثبت سرویس و تعمیرات" else "New Vehicle Service"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Category Selection
                Text(
                    text = if (languageIsFarsi) "نوع سرویس:" else "Service Type:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SERVICE_CATEGORIES.forEach { item ->
                        val isSelected = selectedType == item.key
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .clickable {
                                    selectedType = item.key
                                    if (item.defaultIntervalKm != null && odoInput.isNotBlank() && serviceToEdit == null) {
                                        val odo = odoInput.toDoubleOrNull() ?: 0.0
                                        if (odo > 0) {
                                            nextDueInput = (odo + item.defaultIntervalKm).toLong().toString()
                                        }
                                    }
                                }
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageIsFarsi) item.titleFa else item.titleEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                // Service Date Picker Card (Solar Hijri)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { showDatePicker = true }
                        .testTag("pick_service_date_btn")
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
                                text = if (languageIsFarsi) "تاریخ انجام سرویس (هجری شمسی):" else "Service Date:",
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

                // Current Odometer
                OutlinedTextField(
                    value = odoInput,
                    onValueChange = {
                        odoInput = it
                        odoError = null
                        if (it.isNotBlank()) {
                            val v = it.toDoubleOrNull()
                            if (v == null || v < 0) {
                                odoError = if (languageIsFarsi) "کیلومتر معتبر وارد کنید" else "Enter valid odometer"
                            }
                        }
                    },
                    label = { Text(if (languageIsFarsi) "کیلومتر کارکرد ($distanceUnit) *" else "Odometer ($distanceUnit) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    isError = odoError != null,
                    supportingText = { odoError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("service_odo_input")
                )

                // Next Due Odometer (Optional)
                OutlinedTextField(
                    value = nextDueInput,
                    onValueChange = { nextDueInput = it },
                    label = { Text(if (languageIsFarsi) "کیلومتر موعد سرویس بعدی (اختیاری)" else "Next Due Odometer (Optional)") },
                    supportingText = {
                        Text(
                            if (languageIsFarsi) "برای یادآوری تعویض روغن یا سرویس بعدی" else "Used for maintenance alerts"
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("service_next_due_input")
                )

                // Cost
                OutlinedTextField(
                    value = costInput,
                    onValueChange = {
                        costInput = it
                        costError = null
                        if (it.isNotBlank()) {
                            val c = it.toDoubleOrNull()
                            if (c == null || c <= 0) {
                                costError = if (languageIsFarsi) "هزینه باید بیشتر از صفر باشد" else "Must be greater than zero"
                            }
                        }
                    },
                    label = { Text(if (languageIsFarsi) "هزینه پرداختی ($currencySymbol) *" else "Cost ($currencySymbol) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    isError = costError != null,
                    supportingText = { costError?.let { Text(it) } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("service_cost_input")
                )

                // Provider / Garage
                OutlinedTextField(
                    value = providerInput,
                    onValueChange = { providerInput = it },
                    label = { Text(if (languageIsFarsi) "محل سرویس / نام تعمیرگاه یا نمایندگی" else "Service Center / Shop") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("service_provider_input")
                )

                // Notes
                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text(if (languageIsFarsi) "توضیحات و قطعات تعویض شده" else "Notes / Replaced Parts") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .testTag("service_notes_input")
                )

                // Buttons
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
                            val cost = costInput.toDoubleOrNull()
                            val nextDue = nextDueInput.toDoubleOrNull()

                            if (odo == null || odo < 0) {
                                odoError = if (languageIsFarsi) "کیلومتر نامعتبر است" else "Invalid odometer"
                            }
                            if (cost == null || cost <= 0) {
                                costError = if (languageIsFarsi) "هزینه نامعتبر است" else "Invalid cost"
                            }

                            if (odo != null && cost != null && odoError == null && costError == null) {
                                onSave(
                                    selectedType,
                                    dateInput,
                                    odo,
                                    nextDue,
                                    cost,
                                    providerInput.trim(),
                                    notesInput.trim()
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_service_btn")
                    ) {
                        Text(
                            if (serviceToEdit != null) {
                                if (languageIsFarsi) "ذخیره تغییرات" else "Save Changes"
                            } else {
                                if (languageIsFarsi) "ثبت سرویس" else "Save Service"
                            }
                        )
                    }
                }
            }
        }
    }
}
