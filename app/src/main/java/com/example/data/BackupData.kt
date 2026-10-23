package com.example.data

import org.json.JSONArray
import org.json.JSONObject

data class AppBackupData(
    val version: Int = 1,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val appVersion: String = "1.0",
    val fuelLogs: List<FuelLog>,
    val serviceLogs: List<ServiceLog>
) {
    fun toJsonString(): String {
        val root = JSONObject()
        root.put("version", version)
        root.put("exportTimestamp", exportTimestamp)
        root.put("appVersion", appVersion)

        val fuelsArray = JSONArray()
        fuelLogs.forEach { log ->
            val obj = JSONObject()
            obj.put("id", log.id)
            obj.put("date", log.date)
            obj.put("odometer", log.odometer)
            obj.put("fuelAmount", log.fuelAmount)
            obj.put("totalCost", log.totalCost)
            obj.put("notes", log.notes)
            fuelsArray.put(obj)
        }
        root.put("fuelLogs", fuelsArray)

        val servicesArray = JSONArray()
        serviceLogs.forEach { s ->
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("serviceType", s.serviceType)
            obj.put("date", s.date)
            obj.put("odometer", s.odometer)
            if (s.nextDueOdometer != null) {
                obj.put("nextDueOdometer", s.nextDueOdometer)
            } else {
                obj.put("nextDueOdometer", JSONObject.NULL)
            }
            obj.put("cost", s.cost)
            obj.put("provider", s.provider)
            obj.put("notes", s.notes)
            servicesArray.put(obj)
        }
        root.put("serviceLogs", servicesArray)

        return root.toString(2)
    }

    companion object {
        fun fromJsonString(jsonStr: String): Result<AppBackupData> {
            return runCatching {
                val root = JSONObject(jsonStr)
                val version = root.optInt("version", 1)
                val timestamp = root.optLong("exportTimestamp", System.currentTimeMillis())
                val appVer = root.optString("appVersion", "1.0")

                val fuelList = mutableListOf<FuelLog>()
                val fuelsArray = root.optJSONArray("fuelLogs")
                if (fuelsArray != null) {
                    for (i in 0 until fuelsArray.length()) {
                        val obj = fuelsArray.getJSONObject(i)
                        fuelList.add(
                            FuelLog(
                                id = 0, // Auto-increment or assigned on insert
                                date = obj.getLong("date"),
                                odometer = obj.getDouble("odometer"),
                                fuelAmount = obj.getDouble("fuelAmount"),
                                totalCost = obj.getDouble("totalCost"),
                                notes = obj.optString("notes", "")
                            )
                        )
                    }
                }

                val serviceList = mutableListOf<ServiceLog>()
                val servicesArray = root.optJSONArray("serviceLogs")
                if (servicesArray != null) {
                    for (i in 0 until servicesArray.length()) {
                        val obj = servicesArray.getJSONObject(i)
                        val nextDue = if (obj.isNull("nextDueOdometer") || !obj.has("nextDueOdometer")) {
                            null
                        } else {
                            obj.getDouble("nextDueOdometer")
                        }
                        serviceList.add(
                            ServiceLog(
                                id = 0,
                                serviceType = obj.getString("serviceType"),
                                date = obj.getLong("date"),
                                odometer = obj.getDouble("odometer"),
                                nextDueOdometer = nextDue,
                                cost = obj.getDouble("cost"),
                                provider = obj.optString("provider", ""),
                                notes = obj.optString("notes", "")
                            )
                        )
                    }
                }

                AppBackupData(
                    version = version,
                    exportTimestamp = timestamp,
                    appVersion = appVer,
                    fuelLogs = fuelList,
                    serviceLogs = serviceList
                )
            }
        }
    }
}
