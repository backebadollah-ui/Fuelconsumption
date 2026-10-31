package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelLogDao {
    @Query("SELECT * FROM fuel_logs ORDER BY odometer DESC, date DESC")
    fun getAllLogs(): Flow<List<FuelLog>>

    @Query("SELECT * FROM fuel_logs ORDER BY odometer DESC, date DESC")
    suspend fun getAllLogsSnapshot(): List<FuelLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: FuelLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllLogs(logs: List<FuelLog>)

    @Update
    suspend fun updateLog(log: FuelLog)

    @Delete
    suspend fun deleteLog(log: FuelLog)

    @Query("DELETE FROM fuel_logs WHERE id = :id")
    suspend fun deleteLogById(id: Int)

    @Query("DELETE FROM fuel_logs")
    suspend fun deleteAllLogs()
}
