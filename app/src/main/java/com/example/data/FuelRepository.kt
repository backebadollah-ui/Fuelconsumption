package com.example.data

import kotlinx.coroutines.flow.Flow

class FuelRepository(
    private val fuelLogDao: FuelLogDao,
    private val serviceLogDao: ServiceLogDao
) {
    val allLogs: Flow<List<FuelLog>> = fuelLogDao.getAllLogs()
    val allServices: Flow<List<ServiceLog>> = serviceLogDao.getAllServices()

    suspend fun insert(log: FuelLog) {
        fuelLogDao.insertLog(log)
    }

    suspend fun update(log: FuelLog) {
        fuelLogDao.updateLog(log)
    }

    suspend fun delete(log: FuelLog) {
        fuelLogDao.deleteLog(log)
    }

    suspend fun deleteById(id: Int) {
        fuelLogDao.deleteLogById(id)
    }

    suspend fun insertService(service: ServiceLog) {
        serviceLogDao.insertService(service)
    }

    suspend fun updateService(service: ServiceLog) {
        serviceLogDao.updateService(service)
    }

    suspend fun deleteService(service: ServiceLog) {
        serviceLogDao.deleteService(service)
    }

    suspend fun deleteServiceById(id: Int) {
        serviceLogDao.deleteServiceById(id)
    }
}
