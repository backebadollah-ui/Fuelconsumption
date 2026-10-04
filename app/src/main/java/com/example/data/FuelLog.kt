package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_logs")
data class FuelLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val odometer: Double,
    val fuelAmount: Double,
    val totalCost: Double,
    val notes: String = ""
)
