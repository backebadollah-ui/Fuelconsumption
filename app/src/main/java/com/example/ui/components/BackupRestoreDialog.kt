package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.AppBackupData
import com.example.data.BackupManager
import com.example.data.CloudBackupInfo
import com.example.ui.FuelViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun BackupRestoreDialog(
    viewModel: FuelViewModel,
    languageIsFarsi: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Offline, 1: Online Cloud

    // Sync Key for Cloud
    var cloudSyncKey by remember {
        mutableStateOf(viewModel.getSuggestedSyncKey(context))
    }
    var cloudInfo by remember {
        mutableStateOf<CloudBackupInfo?>(viewModel.getCloudBackupInfo(context, cloudSyncKey))
    }

    // Restore mode: true = Replace all, false = Merge
    var replaceExisting by remember { mutableStateOf(false) }

    // Status banner
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccessStatus by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }

    // Confirm dialog for online restore preview
    var pendingCloudRestoreInfo by remember { mutableStateOf<CloudBackupInfo?>(null) }

    // SAF Create Document launcher (for saving JSON to user's selected folder or Google Drive)
    val saveFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            isLoading = true
            viewModel.exportBackupData { backupData ->
                try {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        BackupManager.writeBackupToStream(backupData, os)
                    }
                    isLoading = false
                    isSuccessStatus = true
                    statusMessage = if (languageIsFarsi)
                        "فایل پشتیبان با موفقیت ذخیره شد (${backupData.fuelLogs.size} سوخت، ${backupData.serviceLogs.size} سرویس)"
                    else
                        "Backup file saved successfully (${backupData.fuelLogs.size} fuels, ${backupData.serviceLogs.size} services)"
                } catch (e: Exception) {
                    isLoading = false
                    isSuccessStatus = false
                    statusMessage = if (languageIsFarsi) "خطا در ذخیره فایل: ${e.message}" else "Error saving file: ${e.message}"
                }
            }
        }
    }

    // SAF Open Document launcher (for reading JSON backup file)
    val openFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isLoading = true
            try {
                val backupResult = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BackupManager.readBackupFromStream(inputStream)
                } ?: Result.failure(Exception("Could not open file stream"))

                val backupData = backupResult.getOrThrow()
                viewModel.restoreBackupData(backupData, replaceExisting) { result ->
                    isLoading = false
                    if (result.isSuccess) {
                        val (fuels, services) = result.getOrThrow()
                        isSuccessStatus = true
                        statusMessage = if (languageIsFarsi)
                            "داده‌ها با موفقیت بازگردانی شدند! ($fuels سوخت، $services سرویس)"
                        else
                            "Successfully restored $fuels fuel logs and $services service logs!"
                    } else {
                        isSuccessStatus = false
                        statusMessage = if (languageIsFarsi)
                            "خطا در بازگردانی داده‌ها: ${result.exceptionOrNull()?.message}"
                        else
                            "Restore error: ${result.exceptionOrNull()?.message}"
                    }
                }
            } catch (e: Exception) {
                isLoading = false
                isSuccessStatus = false
                statusMessage = if (languageIsFarsi)
                    "فایل پشتیبان معتبر نیست: ${e.message}"
                else
                    "Invalid backup file format: ${e.message}"
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.88f)
                .testTag("backup_restore_dialog"),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header with Icon and Title
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF005AC1).copy(alpha = 0.12f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = Color(0xFF005AC1),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (languageIsFarsi) "پشتیبان‌گیری و بازگردانی داده‌ها" else "Backup & Restore",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (languageIsFarsi) "آفلاین و فضای ابری" else "Offline & Cloud Sync",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Navigation Tabs: Offline vs Online Cloud
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clip(RoundedCornerShape(14.dp))
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageIsFarsi) "آفلاین (فایل JSON)" else "Offline (File)",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (languageIsFarsi) "آنلاین (فضای ابری)" else "Online Cloud",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status banner if present
                if (statusMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSuccessStatus) Color(0xFFECFDF5) else Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, if (isSuccessStatus) Color(0xFFA7F3D0) else Color(0xFFFECACA)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isSuccessStatus) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (isSuccessStatus) Color(0xFF059669) else Color(0xFFDC2626),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = statusMessage ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSuccessStatus) Color(0xFF065F46) else Color(0xFF991B1B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Restore strategy toggle (Merge vs Replace)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (languageIsFarsi) "حالت بازگردانی داده‌ها:" else "Restore Mode:",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = if (replaceExisting) {
                                    if (languageIsFarsi) "جایگزینی کامل (حذف رکوردهای قبلی)" else "Replace (Clear existing logs)"
                                } else {
                                    if (languageIsFarsi) "ادغام هوشمند (نگهداری رکوردهای قبلی)" else "Merge (Keep existing logs)"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (replaceExisting) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                        Switch(
                            checked = replaceExisting,
                            onCheckedChange = { replaceExisting = it },
                            modifier = Modifier.testTag("restore_mode_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Main Content Scrollable
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (selectedTab == 0) {
                        // OFFLINE BACKUP & RESTORE SECTION
                        // Section 1: Export
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFF005AC1),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageIsFarsi) "تهیه فایل پشتیبان (خروجی JSON)" else "Export Backup File (JSON)",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (languageIsFarsi)
                                        "شامل کلیه سوابق سوخت‌گیری‌ها، هزینه‌ها و سرویس‌های نگهداری با امکان نگهداری در حافظه گوشی یا اشتراک‌گذاری مستقیم."
                                    else
                                        "Exports all fuel logs, costs, and service records into a portable, standard JSON file.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Save to Storage / Drive
                                    Button(
                                        onClick = {
                                            val df = SimpleDateFormat("yyyyMMdd_HHmm", Locale.US)
                                            val defaultName = "FuelTracker_Backup_${df.format(Date())}.json"
                                            saveFileLauncher.launch(defaultName)
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("save_backup_file_btn"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (languageIsFarsi) "ذخیره در حافظه" else "Save File",
                                            fontSize = 12.sp
                                        )
                                    }

                                    // Direct Share Sheet (Telegram, WhatsApp, Drive, Email)
                                    OutlinedButton(
                                        onClick = {
                                            viewModel.exportBackupData { backup ->
                                                try {
                                                    val (uri, filename) = BackupManager.createShareableBackupUri(context, backup)
                                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                        type = "application/json"
                                                        putExtra(Intent.EXTRA_STREAM, uri)
                                                        putExtra(Intent.EXTRA_SUBJECT, filename)
                                                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                    }
                                                    context.startActivity(Intent.createChooser(sendIntent, if (languageIsFarsi) "ارسال فایل پشتیبان به..." else "Share Backup File"))
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Error sharing: ${e.message}", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("share_backup_file_btn"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (languageIsFarsi) "اشتراک‌گذاری" else "Share File",
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }

                        // Section 2: Import / Restore
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFF0D9488),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageIsFarsi) "بازگردانی از فایل پشتیبان" else "Restore from Backup File",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (languageIsFarsi)
                                        "فایل پشتیبان JSON را انتخاب کنید تا سوابق خودرو دوباره در برنامه بازیابی شوند."
                                    else
                                        "Select a previously exported JSON backup file to restore records to your phone.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        openFileLauncher.launch(arrayOf("application/json", "*/*"))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("open_backup_file_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488))
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageIsFarsi) "انتخاب و بازگردانی فایل JSON" else "Choose & Restore JSON File"
                                    )
                                }
                            }
                        }
                    } else {
                        // ONLINE CLOUD SYNC SECTION
                        // Cloud Sync Key Card
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = if (languageIsFarsi) "کد همگام‌سازی ابری خودرو:" else "Cloud Vehicle Sync Key:",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (languageIsFarsi)
                                        "این کد اختصاصی به شما اجازه می‌دهد داده‌های خودرو را در فضای ابری ذخیره یا در گوشی دیگر بازیابی نمایید."
                                    else
                                        "This unique key identifies your vehicle snapshot in cloud vault for multi-device sync.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = cloudSyncKey,
                                    onValueChange = {
                                        cloudSyncKey = it.uppercase()
                                        cloudInfo = viewModel.getCloudBackupInfo(context, it)
                                    },
                                    label = { Text(if (languageIsFarsi) "کد ابری (مثال: CAR-8821)" else "Sync Key (e.g. CAR-8821)") },
                                    singleLine = true,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            cloudSyncKey = "CAR-" + (1000..9999).random()
                                            cloudInfo = viewModel.getCloudBackupInfo(context, cloudSyncKey)
                                        }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Regenerate")
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("cloud_key_input")
                                )

                                if (cloudInfo != null) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(cloudInfo!!.backupTimestamp))
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFFF1F5F9),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Text(
                                                text = if (languageIsFarsi) "آخرین بکاپ ابری ثبت شده:" else "Last Cloud Backup Registered:",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF1E293B)
                                            )
                                            Text(
                                                text = if (languageIsFarsi)
                                                    "تاریخ: $dateStr • ${cloudInfo!!.fuelCount} سوخت‌گیری • ${cloudInfo!!.serviceCount} سرویس"
                                                else
                                                    "Date: $dateStr • ${cloudInfo!!.fuelCount} fuels • ${cloudInfo!!.serviceCount} services",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFF475569)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Upload to Cloud
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = Color(0xFF005AC1),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageIsFarsi) "پشتیبان‌گیری در فضای ابری" else "Upload to Cloud Vault",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (languageIsFarsi)
                                        "اطلاعات فعلی گوشی شما به عنوان نسخه پشتیبان ابری با کد $cloudSyncKey ذخیره خواهد شد."
                                    else
                                        "Uploads current data to cloud storage under key $cloudSyncKey.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (cloudSyncKey.isBlank()) {
                                            isSuccessStatus = false
                                            statusMessage = if (languageIsFarsi) "لطفاً یک کد ابری معتبر وارد کنید" else "Please enter a valid sync key"
                                            return@Button
                                        }
                                        isLoading = true
                                        viewModel.saveCloudBackup(context, cloudSyncKey) { success ->
                                            isLoading = false
                                            if (success) {
                                                cloudInfo = viewModel.getCloudBackupInfo(context, cloudSyncKey)
                                                isSuccessStatus = true
                                                statusMessage = if (languageIsFarsi)
                                                    "اطلاعات با موفقیت در فضای ابری ذخیره شد (کد: $cloudSyncKey)"
                                                else
                                                    "Successfully saved backup to cloud with key $cloudSyncKey"
                                            } else {
                                                isSuccessStatus = false
                                                statusMessage = if (languageIsFarsi) "خطا در ذخیره ابری" else "Failed to save cloud backup"
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("upload_cloud_backup_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = if (languageIsFarsi) "ذخیره در فضای ابری" else "Save to Cloud")
                                }
                            }
                        }

                        // Restore from Cloud
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = Color(0xFF7C3AED),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (languageIsFarsi) "بازیابی از فضای ابری" else "Restore from Cloud Vault",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (languageIsFarsi)
                                        "فراخوانی و بازگردانی نسخه ذخیره شده از سرور ابری با استفاده از کد $cloudSyncKey."
                                    else
                                        "Retrieve and restore vehicle records stored under key $cloudSyncKey.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontSize = 11.sp
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (cloudSyncKey.isBlank()) {
                                            isSuccessStatus = false
                                            statusMessage = if (languageIsFarsi) "لطفاً کد ابری را وارد کنید" else "Please enter cloud sync key"
                                            return@Button
                                        }
                                        val info = viewModel.getCloudBackupInfo(context, cloudSyncKey)
                                        if (info == null) {
                                            isSuccessStatus = false
                                            statusMessage = if (languageIsFarsi)
                                                "هیچ بکاپ ابری برای کد $cloudSyncKey یافت نشد!"
                                            else
                                                "No cloud backup found for key $cloudSyncKey!"
                                        } else {
                                            pendingCloudRestoreInfo = info
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("restore_cloud_backup_btn"),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = if (languageIsFarsi) "بازیابی نسخه ابری" else "Restore from Cloud")
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Footer close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (languageIsFarsi) "بستن" else "Close")
                    }
                }
            }
        }
    }

    // Modal Confirmation for Cloud Restore Preview
    if (pendingCloudRestoreInfo != null) {
        val info = pendingCloudRestoreInfo!!
        val dateStr = SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault()).format(Date(info.backupTimestamp))

        AlertDialog(
            onDismissRequest = { pendingCloudRestoreInfo = null },
            title = {
                Text(if (languageIsFarsi) "تایید بازیابی از فضای ابری" else "Confirm Cloud Restore")
            },
            text = {
                Column {
                    Text(
                        text = if (languageIsFarsi)
                            "آیا مایلید نسخه ابری با مشخصات زیر بازگردانی شود؟"
                        else
                            "Do you want to restore the following cloud snapshot?"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (languageIsFarsi)
                            "• کد ابری: ${info.syncKey}\n• تاریخ بکاپ: $dateStr\n• تعداد سوخت‌گیری: ${info.fuelCount}\n• تعداد سرویس‌ها: ${info.serviceCount}\n• حالت: ${if (replaceExisting) "جایگزینی داده‌های فعلی" else "ادغام با داده‌های فعلی"}"
                        else
                            "• Key: ${info.syncKey}\n• Timestamp: $dateStr\n• Fuel logs: ${info.fuelCount}\n• Services: ${info.serviceCount}\n• Mode: ${if (replaceExisting) "Replace existing" else "Merge with existing"}",
                        fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val key = info.syncKey
                        pendingCloudRestoreInfo = null
                        isLoading = true
                        viewModel.restoreCloudBackup(context, key, replaceExisting) { result ->
                            isLoading = false
                            if (result.isSuccess) {
                                val (f, s) = result.getOrThrow()
                                isSuccessStatus = true
                                statusMessage = if (languageIsFarsi)
                                    "نسخه ابری با موفقیت بازگردانی شد ($f سوخت، $s سرویس)"
                                else
                                    "Cloud backup successfully restored ($f fuels, $s services)"
                            } else {
                                isSuccessStatus = false
                                statusMessage = if (languageIsFarsi)
                                    "خطا در بازیابی نسخه ابری: ${result.exceptionOrNull()?.message}"
                                else
                                    "Cloud restore error: ${result.exceptionOrNull()?.message}"
                            }
                        }
                    }
                ) {
                    Text(if (languageIsFarsi) "بله، بازیابی شود" else "Confirm Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingCloudRestoreInfo = null }) {
                    Text(if (languageIsFarsi) "انصراف" else "Cancel")
                }
            }
        )
    }
}
