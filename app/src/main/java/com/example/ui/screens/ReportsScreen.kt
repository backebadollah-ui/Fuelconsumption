package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.*
import com.example.ui.components.SERVICE_CATEGORIES
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ReportsScreen(
    reportsData: ReportsData,
    selectedPeriod: ReportPeriod,
    unitSystem: UnitSystem,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    onPeriodSelected: (ReportPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM, UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "کیلومتر" else "km"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "مایل" else "mi"
    }

    val efficiencyUnit = when (unitSystem) {
        UnitSystem.METRIC_L_100KM -> if (languageIsFarsi) "لیتر/۱۰۰ک‌م" else "L/100km"
        UnitSystem.METRIC_KM_L -> if (languageIsFarsi) "ک‌م/لیتر" else "km/L"
        UnitSystem.IMPERIAL_MPG -> if (languageIsFarsi) "MPG" else "MPG"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("reports_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time Period Filter Bar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val periods = listOf(
                        ReportPeriod.ALL to if (languageIsFarsi) "کل تاریخ" else "All Time",
                        ReportPeriod.THREE_MONTHS to if (languageIsFarsi) "۳ ماه اخیر" else "3 Months",
                        ReportPeriod.SIX_MONTHS to if (languageIsFarsi) "۶ ماه اخیر" else "6 Months",
                        ReportPeriod.ONE_YEAR to if (languageIsFarsi) "۱ سال اخیر" else "1 Year"
                    )

                    periods.forEach { (period, label) ->
                        val isSelected = selectedPeriod == period
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onPeriodSelected(period) }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Key Metrics Summary Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Ownership Spend
                    MetricCard(
                        title = if (languageIsFarsi) "کل هزینه‌های خودرو" else "Total Expenses",
                        subtitle = if (languageIsFarsi) "سوخت + سرویس‌ها" else "Fuel + Maintenance",
                        value = String.format(Locale.US, "%,.0f %s", reportsData.totalOverallCost, currencySymbol),
                        icon = Icons.Default.ShoppingCart,
                        accentColor = Color(0xFF005AC1),
                        modifier = Modifier.weight(1f)
                    )

                    // Cost Per Kilometer
                    val costPerKmText = if (reportsData.costPerKm != null) {
                        String.format(Locale.US, "%,.1f %s", reportsData.costPerKm, currencySymbol)
                    } else "---"
                    MetricCard(
                        title = if (languageIsFarsi) "هزینه هر کیلومتر" else "Cost Per $distanceUnit",
                        subtitle = if (languageIsFarsi) "مجموع هزینه / پیمایش" else "Total Cost / Distance",
                        value = costPerKmText,
                        icon = Icons.Default.Place,
                        accentColor = Color(0xFF0D9488),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Avg Fuel Consumption
                    val avgEffText = if (reportsData.avgEfficiency != null) {
                        String.format(Locale.US, "%.2f %s", reportsData.avgEfficiency, efficiencyUnit)
                    } else "---"
                    MetricCard(
                        title = if (languageIsFarsi) "میانگین مصرف" else "Avg Fuel Economy",
                        subtitle = if (languageIsFarsi) "در دوره انتخابی" else "Over Period",
                        value = avgEffText,
                        icon = Icons.Default.Info,
                        accentColor = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f)
                    )

                    // Total Distance
                    MetricCard(
                        title = if (languageIsFarsi) "مسافت پیموده شده" else "Total Distance",
                        subtitle = if (languageIsFarsi) "کیلومتر کل دوره" else "Recorded Travel",
                        value = String.format(Locale.US, "%,.0f %s", reportsData.totalDistance, distanceUnit),
                        icon = Icons.Default.DateRange,
                        accentColor = Color(0xFF7C3AED),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // CHART 1: Fuel Efficiency Trend Line Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("consumption_chart_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (languageIsFarsi) "نمودار تغییرات مصرف سوخت" else "Fuel Consumption Trend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (languageIsFarsi) "روند مصرف در هر بار سوخت‌گیری ($efficiencyUnit)" else "Efficiency per fill-up ($efficiencyUnit)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF005AC1).copy(alpha = 0.12f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF005AC1),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (reportsData.consumptionPoints.size < 2) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize().padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (languageIsFarsi)
                                        "برای رسم نمودار مصرف، حداقل ۲ بار سوخت‌گیری با کیلومتر متوالی نیاز است."
                                    else
                                        "At least 2 consecutive fuel entries are needed to plot the consumption curve.",
                                    style = MaterialTheme.typography.bodySmall,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    } else {
                        // Custom Canvas Line Chart
                        FuelEfficiencyTrendChart(
                            points = reportsData.consumptionPoints,
                            avgEfficiency = reportsData.avgEfficiency ?: 0.0,
                            unit = efficiencyUnit,
                            languageIsFarsi = languageIsFarsi,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Best & Worst stat pills
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val minVal = reportsData.minEfficiency
                            val maxVal = reportsData.maxEfficiency

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFECFDF5),
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (languageIsFarsi) "کمترین مصرف: " else "Min: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF047857),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (minVal != null) String.format(Locale.US, "%.1f %s", minVal, efficiencyUnit) else "---",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF065F46)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFEF2F2),
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (languageIsFarsi) "بیشترین مصرف: " else "Max: ",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFB91C1C),
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (maxVal != null) String.format(Locale.US, "%.1f %s", maxVal, efficiencyUnit) else "---",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF991B1B)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // CHART 2: Monthly Cost Grouped Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_chart_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (languageIsFarsi) "هزینه‌های ماهانه خودرو" else "Monthly Vehicle Costs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = if (languageIsFarsi) "تفکیک هزینه سوخت و سرویس‌ها" else "Fuel vs Service breakdown",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Legend
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFF005AC1), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (languageIsFarsi) "سوخت" else "Fuel", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.size(10.dp).background(Color(0xFFF59E0B), CircleShape))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (languageIsFarsi) "سرویس" else "Service", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (reportsData.monthlyExpenses.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (languageIsFarsi) "اطلاعاتی در این بازه زمانی وجود ندارد" else "No expense records for this period",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    } else {
                        MonthlyCostBarChart(
                            monthlyData = reportsData.monthlyExpenses,
                            currencySymbol = currencySymbol,
                            languageIsFarsi = languageIsFarsi,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(210.dp)
                        )
                    }
                }
            }
        }

        // CHART 3: Expense Category Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("categories_breakdown_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (languageIsFarsi) "سهم هزینه‌های خودرو به تفکیک بخش" else "Expense Distribution by Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = if (languageIsFarsi) "درصد سهم هر بخش از کل مخارج" else "Percentage share of total vehicle spend",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (reportsData.categoryExpenses.isEmpty()) {
                        Text(
                            text = if (languageIsFarsi) "هنوز رکوردی ثبت نشده است." else "No records available.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    } else {
                        // Multi-color distribution bar
                        MultiColorDistributionBar(
                            categories = reportsData.categoryExpenses,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(16.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category items with progress and amounts
                        reportsData.categoryExpenses.forEach { cat ->
                            CategoryProgressRow(
                                category = cat,
                                currencySymbol = currencySymbol,
                                languageIsFarsi = languageIsFarsi
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    subtitle: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = CircleShape,
                    color = accentColor.copy(alpha = 0.12f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(15.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                fontSize = 9.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FuelEfficiencyTrendChart(
    points: List<ConsumptionDataPoint>,
    avgEfficiency: Double,
    unit: String,
    languageIsFarsi: Boolean,
    modifier: Modifier = Modifier
) {
    val strokeColor = Color(0xFF005AC1)
    val avgLineColor = Color(0xFF94A3B8)

    Canvas(modifier = modifier.padding(vertical = 12.dp, horizontal = 16.dp)) {
        if (points.size < 2) return@Canvas

        val w = size.width
        val h = size.height
        val paddingBottom = 20.dp.toPx()
        val paddingTop = 15.dp.toPx()
        val usableHeight = h - paddingBottom - paddingTop

        val values = points.map { it.efficiency }
        val minVal = (values.minOrNull() ?: 0.0) * 0.85
        val maxVal = (values.maxOrNull() ?: 1.0) * 1.15
        val range = (maxVal - minVal).coerceAtLeast(0.1)

        fun getY(value: Double): Float {
            val fraction = (value - minVal) / range
            return (h - paddingBottom - (fraction * usableHeight)).toFloat()
        }

        fun getX(index: Int): Float {
            return (index.toFloat() / (points.size - 1)) * w
        }

        // Draw average dashed line
        if (avgEfficiency in minVal..maxVal) {
            val avgY = getY(avgEfficiency)
            drawLine(
                color = avgLineColor,
                start = Offset(0f, avgY),
                end = Offset(w, avgY),
                strokeWidth = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
            )
        }

        // Build path for smooth bezier curve and filled area
        val linePath = Path()
        val fillPath = Path()

        val startX = getX(0)
        val startY = getY(values[0])

        linePath.moveTo(startX, startY)
        fillPath.moveTo(startX, h - paddingBottom)
        fillPath.lineTo(startX, startY)

        for (i in 0 until points.size - 1) {
            val p0X = getX(i)
            val p0Y = getY(values[i])
            val p1X = getX(i + 1)
            val p1Y = getY(values[i + 1])

            val ctrl1X = p0X + (p1X - p0X) / 2
            val ctrl1Y = p0Y
            val ctrl2X = p0X + (p1X - p0X) / 2
            val ctrl2Y = p1Y

            linePath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, p1X, p1Y)
            fillPath.cubicTo(ctrl1X, ctrl1Y, ctrl2X, ctrl2Y, p1X, p1Y)
        }

        fillPath.lineTo(getX(points.size - 1), h - paddingBottom)
        fillPath.close()

        // Draw gradient fill
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(strokeColor.copy(alpha = 0.25f), Color.Transparent),
                startY = paddingTop,
                endY = h - paddingBottom
            )
        )

        // Draw curve line
        drawPath(
            path = linePath,
            color = strokeColor,
            style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
        )

        // Draw points
        for (i in points.indices) {
            val px = getX(i)
            val py = getY(values[i])

            // Outer circle
            drawCircle(
                color = strokeColor,
                radius = 5.5.dp.toPx(),
                center = Offset(px, py)
            )
            // Inner circle
            drawCircle(
                color = Color.White,
                radius = 3.dp.toPx(),
                center = Offset(px, py)
            )
        }
    }
}

@Composable
fun MonthlyCostBarChart(
    monthlyData: List<MonthlyExpenseItem>,
    currencySymbol: String,
    languageIsFarsi: Boolean,
    modifier: Modifier = Modifier
) {
    val fuelColor = Color(0xFF005AC1)
    val serviceColor = Color(0xFFF59E0B)

    Canvas(modifier = modifier.padding(vertical = 8.dp, horizontal = 8.dp)) {
        if (monthlyData.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val paddingBottom = 28.dp.toPx()
        val paddingTop = 12.dp.toPx()
        val usableHeight = h - paddingBottom - paddingTop

        val maxMonthTotal = monthlyData.maxOfOrNull { it.totalCost } ?: 1.0
        val scale = if (maxMonthTotal > 0) maxMonthTotal * 1.15 else 1.0

        val count = monthlyData.size
        val groupWidth = w / count
        val barWidth = (groupWidth * 0.32f).coerceAtMost(24.dp.toPx())

        for (i in 0 until count) {
            val item = monthlyData[i]
            val groupCenterX = (i * groupWidth) + (groupWidth / 2)

            val fuelHeight = ((item.fuelCost / scale) * usableHeight).toFloat()
            val serviceHeight = ((item.serviceCost / scale) * usableHeight).toFloat()

            val fuelLeft = groupCenterX - barWidth - 2.dp.toPx()
            val serviceLeft = groupCenterX + 2.dp.toPx()

            val baseBottom = h - paddingBottom

            // Draw fuel bar
            if (fuelHeight > 0) {
                drawRoundRect(
                    color = fuelColor,
                    topLeft = Offset(fuelLeft, baseBottom - fuelHeight),
                    size = Size(barWidth, fuelHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Draw service bar
            if (serviceHeight > 0) {
                drawRoundRect(
                    color = serviceColor,
                    topLeft = Offset(serviceLeft, baseBottom - serviceHeight),
                    size = Size(barWidth, serviceHeight),
                    cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                )
            }

            // Base tick
            drawLine(
                color = Color(0xFFE2E8F0),
                start = Offset(0f, baseBottom),
                end = Offset(w, baseBottom),
                strokeWidth = 1.dp.toPx()
            )
        }
    }

    // Month labels row below Canvas
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        monthlyData.forEach { item ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = item.displayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = String.format(Locale.US, "%,.0f", item.totalCost),
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
fun MultiColorDistributionBar(
    categories: List<CategoryExpense>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFFF1F5F9))
    ) {
        categories.forEach { cat ->
            if (cat.percentage > 0.01f) {
                val color = getCategoryColor(cat.category)
                Box(
                    modifier = Modifier
                        .weight(cat.percentage)
                        .fillMaxHeight()
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun CategoryProgressRow(
    category: CategoryExpense,
    currencySymbol: String,
    languageIsFarsi: Boolean
) {
    val color = getCategoryColor(category.category)
    val catName = if (category.category == "FUEL") {
        if (languageIsFarsi) "سوخت خودرو" else "Fuel"
    } else {
        val item = SERVICE_CATEGORIES.firstOrNull { it.key == category.category }
        if (languageIsFarsi) (item?.titleFa ?: category.category) else (item?.titleEn ?: category.category)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(3.dp)))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = catName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = String.format(Locale.US, "%,.0f %s", category.amount, currencySymbol),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = color.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.0f%%", category.percentage * 100),
                        style = MaterialTheme.typography.labelSmall,
                        color = color,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { category.percentage },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = Color(0xFFF1F5F9)
        )
    }
}

fun getCategoryColor(categoryKey: String): Color {
    return when (categoryKey) {
        "FUEL" -> Color(0xFF005AC1)
        "OIL" -> Color(0xFFD97706)
        "REPAIR" -> Color(0xFFDC2626)
        "INSURANCE" -> Color(0xFF2563EB)
        "TIRE" -> Color(0xFF059669)
        "INSPECTION" -> Color(0xFF7C3AED)
        "WASH" -> Color(0xFF0284C7)
        else -> Color(0xFF6B7280)
    }
}
