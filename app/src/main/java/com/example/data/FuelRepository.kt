package com.example.data

import kotlinx.coroutines.flow.Flow

class FuelRepository(private val fuelLogDao: FuelLogDao) {
    val allLogs: Flow<List<FuelLog>> = fuelLogDao.getAllLogs()

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
}
