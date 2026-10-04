package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FuelLog
import com.example.data.FuelRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class UnitSystem {
    METRIC_L_100KM, // Liters per 100 km
    METRIC_KM_L,    // Kilometers per Liter
    IMPERIAL_MPG    // Miles per Gallons
}

data class FuelLogUiModel(
    val log: FuelLog,
    val distanceTraveled: Double?, // distance since previous log
    val efficiency: Double?, // in preferred unit system
    val pricePerUnit: Double // cost / fuelAmount
)

data class Statistics(
    val totalCost: Double,
    val totalFuel: Double,
    val totalDistance: Double,
    val averageEfficiency: Double?,
    val averagePricePerUnit: Double
)

class FuelViewModel(private val repository: FuelRepository) : ViewModel() {

    private val _unitSystem = MutableStateFlow(UnitSystem.METRIC_L_100KM)
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    private val _currencySymbol = MutableStateFlow("$")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    val logsList: StateFlow<List<FuelLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val uiLogsList: StateFlow<List<FuelLogUiModel>> = combine(logsList, unitSystem) { logs, system ->
        calculateUiLogs(logs, system)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val statistics: StateFlow<Statistics> = combine(logsList, unitSystem) { logs, system ->
        calculateStatistics(logs, system)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Statistics(0.0, 0.0, 0.0, null, 0.0)
    )

    fun setUnitSystem(system: UnitSystem) {
        _unitSystem.value = system
    }

    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun insertLog(odometer: Double, fuelAmount: Double, totalCost: Double, date: Long, notes: String) {
        viewModelScope.launch {
            repository.insert(
                FuelLog(
                    id = 0,
                    date = date,
                    odometer = odometer,
                    fuelAmount = fuelAmount,
                    totalCost = totalCost,
                    notes = notes
                )
            )
        }
    }

    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    private fun calculateUiLogs(logs: List<FuelLog>, system: UnitSystem): List<FuelLogUiModel> {
        if (logs.isEmpty()) return emptyList()
        
        return logs.mapIndexed { index, current ->
            val prevLog = if (index < logs.size - 1) logs[index + 1] else null
            val distance = if (prevLog != null) {
                current.odometer - prevLog.odometer
            } else {
                null
            }

            val pricePerUnit = if (current.fuelAmount > 0) current.totalCost / current.fuelAmount else 0.0

            val efficiency = if (distance != null && distance > 0 && current.fuelAmount > 0) {
                when (system) {
                    UnitSystem.METRIC_L_100KM -> {
                        (current.fuelAmount / distance) * 100
                    }
                    UnitSystem.METRIC_KM_L -> {
                        distance / current.fuelAmount
                    }
                    UnitSystem.IMPERIAL_MPG -> {
                        distance / current.fuelAmount
                    }
                }
            } else {
                null
            }

            FuelLogUiModel(
                log = current,
                distanceTraveled = distance,
                efficiency = efficiency,
                pricePerUnit = pricePerUnit
            )
        }
    }

    private fun calculateStatistics(logs: List<FuelLog>, system: UnitSystem): Statistics {
        if (logs.isEmpty()) return Statistics(0.0, 0.0, 0.0, null, 0.0)

        val totalCost = logs.sumOf { it.totalCost }
        val totalFuel = logs.sumOf { it.fuelAmount }

        val maxOdometer = logs.maxOf { it.odometer }
        val minOdometer = logs.minOf { it.odometer }
        val totalDistance = maxOdometer - minOdometer

        val sortedLogs = logs.sortedBy { it.odometer }
        val averageEfficiency = if (totalDistance > 0 && sortedLogs.size > 1) {
            val intervalFuel = sortedLogs.drop(1).sumOf { it.fuelAmount }
            when (system) {
                UnitSystem.METRIC_L_100KM -> {
                    (intervalFuel / totalDistance) * 100
                }
                UnitSystem.METRIC_KM_L -> {
                    totalDistance / intervalFuel
                }
                UnitSystem.IMPERIAL_MPG -> {
                    totalDistance / intervalFuel
                }
            }
        } else {
            null
        }

        val avgPricePerUnit = if (totalFuel > 0) totalCost / totalFuel else 0.0

        return Statistics(
            totalCost = totalCost,
            totalFuel = totalFuel,
            totalDistance = totalDistance,
            averageEfficiency = averageEfficiency,
            averagePricePerUnit = avgPricePerUnit
        )
    }
}

class FuelViewModelFactory(private val repository: FuelRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FuelViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FuelViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
