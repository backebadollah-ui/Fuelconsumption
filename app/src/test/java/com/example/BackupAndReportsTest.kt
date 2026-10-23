package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.*
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BackupAndReportsTest {

    @Test
    fun testAppBackupDataJsonSerializationRoundtrip() {
        val fuels = listOf(
            FuelLog(id = 1, date = 1700000000000L, odometer = 50000.0, fuelAmount = 35.5, totalCost = 55000.0, notes = "Tehran"),
            FuelLog(id = 2, date = 1700500000000L, odometer = 50500.0, fuelAmount = 30.0, totalCost = 45000.0, notes = "Karaj")
        )

        val services = listOf(
            ServiceLog(id = 1, serviceType = "OIL", date = 1700100000000L, odometer = 50100.0, nextDueOdometer = 55000.0, cost = 250000.0, provider = "Total Shop", notes = "5W30"),
            ServiceLog(id = 2, serviceType = "INSURANCE", date = 1700200000000L, odometer = 50200.0, nextDueOdometer = null, cost = 800000.0, provider = "Iran Insurance", notes = "1 Year")
        )

        val backup = AppBackupData(
            version = 1,
            exportTimestamp = 1701000000000L,
            appVersion = "1.0",
            fuelLogs = fuels,
            serviceLogs = services
        )

        val json = backup.toJsonString()
        assertTrue("JSON should contain fuel logs", json.contains("50000"))
        assertTrue("JSON should contain service logs", json.contains("Iran Insurance"))

        val parsedResult = AppBackupData.fromJsonString(json)
        assertTrue("JSON parsing should succeed", parsedResult.isSuccess)

        val restored = parsedResult.getOrThrow()
        assertEquals(2, restored.fuelLogs.size)
        assertEquals(2, restored.serviceLogs.size)
        assertEquals(50000.0, restored.fuelLogs[0].odometer, 0.01)
        assertEquals(55000.0, restored.serviceLogs[0].nextDueOdometer ?: 0.0, 0.01)
        assertNull(restored.serviceLogs[1].nextDueOdometer)
    }

    @Test
    fun testCloudBackupSaveAndRestore() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val syncKey = "TEST-CAR-1234"

        val backup = AppBackupData(
            version = 1,
            exportTimestamp = System.currentTimeMillis(),
            appVersion = "1.0",
            fuelLogs = listOf(FuelLog(id = 0, date = 1000L, odometer = 10000.0, fuelAmount = 20.0, totalCost = 30000.0)),
            serviceLogs = listOf(ServiceLog(id = 0, serviceType = "OIL", date = 1000L, odometer = 10000.0, nextDueOdometer = 15000.0, cost = 100000.0))
        )

        val saveOk = BackupManager.saveCloudBackup(context, syncKey, backup)
        assertTrue(saveOk)

        val info = BackupManager.getCloudBackupInfo(context, syncKey)
        assertNotNull(info)
        assertEquals(1, info?.fuelCount)
        assertEquals(1, info?.serviceCount)
        assertEquals(syncKey, info?.syncKey)

        val restoredResult = BackupManager.getCloudBackup(context, syncKey)
        assertTrue(restoredResult.isSuccess)
        val restored = restoredResult.getOrThrow()
        assertEquals(1, restored.fuelLogs.size)
        assertEquals(10000.0, restored.fuelLogs[0].odometer, 0.01)
    }
}
