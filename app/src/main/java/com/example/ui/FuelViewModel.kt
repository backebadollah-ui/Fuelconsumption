package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.FuelLog
import com.example.data.FuelRepository
import com.example.data.ServiceLog
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class UnitSystem {
    METRIC_L_100KM, // Liters per 100 km
    METRIC_KM_L,    // Kilometers per Liter
    IMPERIAL_MPG    // Miles per Gallons
}

enum class ReportPeriod {
    ALL,
    THREE_MONTHS,
    SIX_MONTHS,
    ONE_YEAR
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

data class ServiceAlert(
    val serviceType: String,
    val nextDueOdometer: Double,
    val kmRemaining: Double,
    val isOverdue: Boolean
)

data class ServiceStatistics(
    val totalServiceCost: Double,
    val serviceCount: Int,
    val costByType: Map<String, Double>,
    val alerts: List<ServiceAlert>
)

data class ConsumptionDataPoint(
    val logId: Int,
    val date: Long,
    val odometer: Double,
    val efficiency: Double,
    val distance: Double
)

data class MonthlyExpenseItem(
    val monthKey: String, // e.g. "2024-05"
    val displayLabel: String,
    val fuelCost: Double,
    val serviceCost: Double,
    val totalCost: Double
)

data class CategoryExpense(
    val category: String,
    val amount: Double,
    val percentage: Float
)

data class ReportsData(
    val consumptionPoints: List<ConsumptionDataPoint>,
    val minEfficiency: Double?,
    val maxEfficiency: Double?,
    val avgEfficiency: Double?,
    val monthlyExpenses: List<MonthlyExpenseItem>,
    val categoryExpenses: List<CategoryExpense>,
    val totalFuelCost: Double,
    val totalServiceCost: Double,
    val totalOverallCost: Double,
    val totalDistance: Double,
    val costPerKm: Double?
)

class FuelViewModel(private val repository: FuelRepository) : ViewModel() {

    private val _unitSystem = MutableStateFlow(UnitSystem.METRIC_L_100KM)
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    private val _currencySymbol = MutableStateFlow("تومان")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _reportPeriod = MutableStateFlow(ReportPeriod.ALL)
    val reportPeriod: StateFlow<ReportPeriod> = _reportPeriod.asStateFlow()

    val logsList: StateFlow<List<FuelLog>> = repository.allLogs
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val servicesList: StateFlow<List<ServiceLog>> = repository.allServices
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

    val serviceStatistics: StateFlow<ServiceStatistics> = combine(servicesList, logsList) { services, fuels ->
        calculateServiceStatistics(services, fuels)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ServiceStatistics(0.0, 0, emptyMap(), emptyList())
    )

    val reportsData: StateFlow<ReportsData> = combine(
        uiLogsList,
        servicesList,
        unitSystem,
        reportPeriod
    ) { uiLogs, services, system, period ->
        calculateReportsData(uiLogs, services, system, period)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportsData(
            consumptionPoints = emptyList(),
            minEfficiency = null,
            maxEfficiency = null,
            avgEfficiency = null,
            monthlyExpenses = emptyList(),
            categoryExpenses = emptyList(),
            totalFuelCost = 0.0,
            totalServiceCost = 0.0,
            totalOverallCost = 0.0,
            totalDistance = 0.0,
            costPerKm = null
        )
    )

    fun setUnitSystem(system: UnitSystem) {
        _unitSystem.value = system
    }

    fun setCurrencySymbol(symbol: String) {
        _currencySymbol.value = symbol
    }

    fun setReportPeriod(period: ReportPeriod) {
        _reportPeriod.value = period
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

    fun updateLog(log: FuelLog) {
        viewModelScope.launch {
            repository.update(log)
        }
    }

    fun deleteLog(id: Int) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    fun updateService(service: ServiceLog) {
        viewModelScope.launch {
            repository.updateService(service)
        }
    }

    fun insertService(
        serviceType: String,
        date: Long,
        odometer: Double,
        nextDueOdometer: Double?,
        cost: Double,
        provider: String,
        notes: String
    ) {
        viewModelScope.launch {
            repository.insertService(
                ServiceLog(
                    id = 0,
                    serviceType = serviceType,
                    date = date,
                    odometer = odometer,
                    nextDueOdometer = nextDueOdometer,
                    cost = cost,
                    provider = provider,
                    notes = notes
                )
            )
        }
    }

    fun deleteService(id: Int) {
        viewModelScope.launch {
            repository.deleteServiceById(id)
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
        val totalDistance = (maxOdometer - minOdometer).coerceAtLeast(0.0)

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

    private fun calculateServiceStatistics(
        services: List<ServiceLog>,
        fuelLogs: List<FuelLog>
    ): ServiceStatistics {
        val totalCost = services.sumOf { it.cost }
        val costByType = services.groupBy { it.serviceType }
            .mapValues { entry -> entry.value.sumOf { it.cost } }

        // Find current maximum odometer reading to check next due services
        val maxFuelOdo = fuelLogs.maxOfOrNull { it.odometer } ?: 0.0
        val maxServiceOdo = services.maxOfOrNull { it.odometer } ?: 0.0
        val currentOdo = maxOf(maxFuelOdo, maxServiceOdo)

        val alerts = services
            .filter { it.nextDueOdometer != null && it.nextDueOdometer > 0 }
            .map { service ->
                val due = service.nextDueOdometer ?: 0.0
                val remaining = due - currentOdo
                ServiceAlert(
                    serviceType = service.serviceType,
                    nextDueOdometer = due,
                    kmRemaining = remaining,
                    isOverdue = remaining <= 0
                )
            }
            .sortedBy { it.kmRemaining }

        return ServiceStatistics(
            totalServiceCost = totalCost,
            serviceCount = services.size,
            costByType = costByType,
            alerts = alerts
        )
    }

    private fun calculateReportsData(
        uiLogs: List<FuelLogUiModel>,
        services: List<ServiceLog>,
        system: UnitSystem,
        period: ReportPeriod
    ): ReportsData {
        val now = System.currentTimeMillis()
        val filterTime: Long = when (period) {
            ReportPeriod.ALL -> 0L
            ReportPeriod.THREE_MONTHS -> now - (90L * 24 * 3600 * 1000)
            ReportPeriod.SIX_MONTHS -> now - (180L * 24 * 3600 * 1000)
            ReportPeriod.ONE_YEAR -> now - (365L * 24 * 3600 * 1000)
        }

        val filteredUiLogs = uiLogs.filter { it.log.date >= filterTime }
        val filteredServices = services.filter { it.date >= filterTime }

        // Consumption trend points (chronological order)
        val validPoints = filteredUiLogs
            .filter { it.efficiency != null && it.efficiency > 0 }
            .map {
                ConsumptionDataPoint(
                    logId = it.log.id,
                    date = it.log.date,
                    odometer = it.log.odometer,
                    efficiency = it.efficiency ?: 0.0,
                    distance = it.distanceTraveled ?: 0.0
                )
            }
            .sortedBy { it.date }

        val minEff = validPoints.minOfOrNull { it.efficiency }
        val maxEff = validPoints.maxOfOrNull { it.efficiency }
        val avgEff = if (validPoints.isNotEmpty()) validPoints.map { it.efficiency }.average() else null

        // Monthly breakdown
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val displayMonthFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        val fuelByMonth = filteredUiLogs.groupBy { monthFormat.format(Date(it.log.date)) }
        val serviceByMonth = filteredServices.groupBy { monthFormat.format(Date(it.date)) }

        val allMonthKeys = (fuelByMonth.keys + serviceByMonth.keys).sorted()

        val monthlyExpenses = allMonthKeys.map { key ->
            val fuelSum = fuelByMonth[key]?.sumOf { it.log.totalCost } ?: 0.0
            val serviceSum = serviceByMonth[key]?.sumOf { it.cost } ?: 0.0
            val parsedDate = runCatching { monthFormat.parse(key) }.getOrNull()
            val label = if (parsedDate != null) displayMonthFormat.format(parsedDate) else key

            MonthlyExpenseItem(
                monthKey = key,
                displayLabel = label,
                fuelCost = fuelSum,
                serviceCost = serviceSum,
                totalCost = fuelSum + serviceSum
            )
        }

        // Category breakdown
        val totalFuelCost = filteredUiLogs.sumOf { it.log.totalCost }
        val totalServiceCost = filteredServices.sumOf { it.cost }
        val totalOverall = totalFuelCost + totalServiceCost

        val categoriesList = mutableListOf<CategoryExpense>()
        if (totalFuelCost > 0) {
            val pct = if (totalOverall > 0) (totalFuelCost / totalOverall).toFloat() else 0f
            categoriesList.add(CategoryExpense("FUEL", totalFuelCost, pct))
        }

        val serviceTypeMap = filteredServices.groupBy { it.serviceType }
        serviceTypeMap.forEach { (type, list) ->
            val sum = list.sumOf { it.cost }
            val pct = if (totalOverall > 0) (sum / totalOverall).toFloat() else 0f
            categoriesList.add(CategoryExpense(type, sum, pct))
        }

        // Distance & cost per km
        val minOdo = filteredUiLogs.minOfOrNull { it.log.odometer } ?: 0.0
        val maxOdo = filteredUiLogs.maxOfOrNull { it.log.odometer } ?: 0.0
        val totalDist = (maxOdo - minOdo).coerceAtLeast(0.0)

        val costPerKm = if (totalDist > 0 && totalOverall > 0) {
            totalOverall / totalDist
        } else null

        return ReportsData(
            consumptionPoints = validPoints,
            minEfficiency = minEff,
            maxEfficiency = maxEff,
            avgEfficiency = avgEff,
            monthlyExpenses = monthlyExpenses,
            categoryExpenses = categoriesList.sortedByDescending { it.amount },
            totalFuelCost = totalFuelCost,
            totalServiceCost = totalServiceCost,
            totalOverallCost = totalOverall,
            totalDistance = totalDist,
            costPerKm = costPerKm
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
