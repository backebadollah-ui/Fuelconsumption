package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "service_logs")
data class ServiceLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceType: String, // "OIL", "REPAIR", "INSURANCE", "TIRE", "INSPECTION", "WASH", "OTHER"
    val date: Long,
    val odometer: Double,
    val nextDueOdometer: Double? = null,
    val cost: Double,
    val provider: String = "",
    val notes: String = ""
)
