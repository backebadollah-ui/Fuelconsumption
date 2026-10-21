package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [FuelLog::class, ServiceLog::class], version = 2, exportSchema = false)
abstract class FuelDatabase : RoomDatabase() {
    abstract fun fuelLogDao(): FuelLogDao
    abstract fun serviceLogDao(): ServiceLogDao

    companion object {
        @Volatile
        private var INSTANCE: FuelDatabase? = null

        fun getDatabase(context: Context): FuelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FuelDatabase::class.java,
                    "fuel_database"
                )
                .fallbackToDestructiveMigration(true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
