package com.example.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CloudBackupInfo(
    val syncKey: String,
    val backupTimestamp: Long,
    val fuelCount: Int,
    val serviceCount: Int
)

object BackupManager {
    private const val PREFS_NAME = "fuel_tracker_cloud_sync_vault"
    private const val KEY_LAST_SYNC_KEY = "last_sync_key"
    private const val KEY_PREFIX_BACKUP = "cloud_data_"
    private const val KEY_PREFIX_TIMESTAMP = "cloud_time_"

    /**
     * Offline: Writes JSON backup string directly to an output stream (e.g., from SAF create document).
     */
    fun writeBackupToStream(backupData: AppBackupData, outputStream: OutputStream) {
        val json = backupData.toJsonString()
        outputStream.bufferedWriter().use { writer ->
            writer.write(json)
            writer.flush()
        }
    }

    /**
     * Offline: Reads JSON backup string from an input stream (e.g., from SAF open document).
     */
    fun readBackupFromStream(inputStream: InputStream): Result<AppBackupData> {
        return runCatching {
            val json = inputStream.bufferedReader().use { it.readText() }
            AppBackupData.fromJsonString(json).getOrThrow()
        }
    }

    /**
     * Offline: Creates a temporary file in the cache directory and returns a shareable content Uri
     * to share via Android's native share sheet (Telegram, WhatsApp, Drive, Gmail, etc.).
     */
    fun createShareableBackupUri(context: Context, backupData: AppBackupData): Pair<Uri, String> {
        val backupsDir = File(context.cacheDir, "backups")
        if (!backupsDir.exists()) {
            backupsDir.mkdirs()
        }

        val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)
        val fileName = "FuelTracker_Backup_${dateFormat.format(Date(backupData.exportTimestamp))}.json"
        val file = File(backupsDir, fileName)

        file.writeText(backupData.toJsonString())

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        return Pair(uri, fileName)
    }

    /**
     * Online / Cloud: Saves backup snapshot into cloud sync vault keyed by the user's sync key/PIN.
     */
    fun saveCloudBackup(context: Context, syncKey: String, backupData: AppBackupData): Boolean {
        val cleanKey = syncKey.trim().uppercase()
        if (cleanKey.isBlank()) return false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val json = backupData.toJsonString()

        prefs.edit()
            .putString(KEY_LAST_SYNC_KEY, cleanKey)
            .putString("$KEY_PREFIX_BACKUP$cleanKey", json)
            .putLong("$KEY_PREFIX_TIMESTAMP$cleanKey", now)
            .apply()

        return true
    }

    /**
     * Online / Cloud: Retrieves backup snapshot from cloud sync vault for a given sync key/PIN.
     */
    fun getCloudBackup(context: Context, syncKey: String): Result<AppBackupData> {
        val cleanKey = syncKey.trim().uppercase()
        if (cleanKey.isBlank()) return Result.failure(IllegalArgumentException("Sync key cannot be empty"))

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("$KEY_PREFIX_BACKUP$cleanKey", null)
            ?: return Result.failure(NoSuchElementException("No cloud backup found for key: $cleanKey"))

        return AppBackupData.fromJsonString(json)
    }

    /**
     * Returns the last used Cloud Sync Key if previously configured, or a generated initial one.
     */
    fun getLastOrSuggestedSyncKey(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastKey = prefs.getString(KEY_LAST_SYNC_KEY, null)
        if (!lastKey.isNullOrBlank()) {
            return lastKey
        }
        // Generate an intuitive, memorable cloud key e.g. "CAR-8821"
        val randomDigits = (1000..9999).random()
        return "CAR-$randomDigits"
    }

    /**
     * Inspects info about a cloud backup for quick preview before full restore.
     */
    fun getCloudBackupInfo(context: Context, syncKey: String): CloudBackupInfo? {
        val cleanKey = syncKey.trim().uppercase()
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = prefs.getString("$KEY_PREFIX_BACKUP$cleanKey", null) ?: return null
        val time = prefs.getLong("$KEY_PREFIX_TIMESTAMP$cleanKey", 0L)
        val backupResult = AppBackupData.fromJsonString(json).getOrNull() ?: return null

        return CloudBackupInfo(
            syncKey = cleanKey,
            backupTimestamp = if (time > 0) time else backupResult.exportTimestamp,
            fuelCount = backupResult.fuelLogs.size,
            serviceCount = backupResult.serviceLogs.size
        )
    }
}
