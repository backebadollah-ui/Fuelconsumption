package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceLogDao {
    @Query("SELECT * FROM service_logs ORDER BY date DESC, odometer DESC")
    fun getAllServices(): Flow<List<ServiceLog>>

    @Query("SELECT * FROM service_logs ORDER BY date DESC, odometer DESC")
    suspend fun getAllServicesSnapshot(): List<ServiceLog>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllServices(services: List<ServiceLog>)

    @Update
    suspend fun updateService(service: ServiceLog)

    @Delete
    suspend fun deleteService(service: ServiceLog)

    @Query("DELETE FROM service_logs WHERE id = :id")
    suspend fun deleteServiceById(id: Int)

    @Query("DELETE FROM service_logs")
    suspend fun deleteAllServices()
}
