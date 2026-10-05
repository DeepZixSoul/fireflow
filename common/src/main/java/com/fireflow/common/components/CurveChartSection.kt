package com.fireflow.common.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fireflow.common.R
import com.fireflow.domain.model.Motor
import com.fireflow.domain.model.PressureMeasurement
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoScrollState
import com.patrykandpatrick.vico.compose.cartesian.rememberVicoZoomState
import com.patrykandpatrick.vico.core.cartesian.CartesianDrawingContext
import com.patrykandpatrick.vico.core.cartesian.CartesianMeasuringContext
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.layer.CartesianLayerDimensions
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.Locale

data class CurveChartPoint(val pct: Int, val flow: Double, val pressure: Double?)

fun buildCurveData(motor: Motor, measurement: PressureMeasurement): List<CurveChartPoint> {
    val flowLmin = motor.nominalFlow * 1000.0 / 60.0
    val percentages = listOf(0, 50, 100, 140, 200)
    val pressures = listOf(measurement.pressureAt0, measurement.pressureAt50, measurement.pressureAt100, measurement.pressureAt140, null)

    return percentages.zip(pressures).map { (pct, pressure) ->
        val flow = flowLmin * pct / 100.0
        val roundedFlow = BigDecimal(flow).setScale(2, RoundingMode.HALF_UP).toDouble()
        CurveChartPoint(
            pct = pct,
            flow = roundedFlow,
            pressure = pressure?.let { BigDecimal(it).setScale(2, RoundingMode.HALF_UP).toDouble() }
        )
    }
}

fun hasCurveData(measurement: PressureMeasurement): Boolean {
    return measurement.pressureAt0 != null || measurement.pressureAt50 != null ||
        measurement.pressureAt100 != null || measurement.pressureAt140 != null
}

fun formatFlow(flow: Double): String = if (flow == flow.toLong().toDouble()) flow.toLong().toString() else String.format("%.1f", flow)
fun formatPressure(pressure: Double): String = String.format("%.2f", pressure)

@Composable
fun CurveChartSection(
    motors: List<Motor>,
    measurements: Map<Long, PressureMeasurement?>
) {
    var selectedMotorId by remember { mutableStateOf<Long?>(null) }

    val motor = if (selectedMotorId != null) {
        motors.find { it.id == selectedMotorId }
    } else {
        motors.firstOrNull()
    }
    val measurement = motor?.id?.let { measurements[it] }

    HorizontalDivider()
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = stringResource(R.string.curve_chart_title),
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )

    if (motors.size > 1) {
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            motors.forEach { m ->
                val isSelected = m.id == (selectedMotorId ?: motors.first().id)
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedMotorId = m.id },
                    label = {
                        val typeLabelRes = if (m.motorType.name == "ELECTRIC") R.string.grupos_motor_type_electric else R.string.grupos_motor_type_diesel
                        Text(stringResource(R.string.curve_chart_motor_label, stringResource(typeLabelRes)))
                    }
                )
            }
        }
    }

    if (motor != null && measurement != null && hasCurveData(measurement)) {
        Spacer(modifier = Modifier.height(8.dp))
        CurveChart(motor = motor, measurement = measurement)
    } else {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.curve_chart_no_data),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CurveChart(motor: Motor, measurement: PressureMeasurement) {
    val curveData = remember(motor, measurement) { buildCurveData(motor, measurement) }

    val modelProducer = remember { CartesianChartModelProducer() }
    val chartPoints = remember(curveData) {
        curveData.filter { it.pressure != null }
    }

    val flowLmin = remember(motor) { motor.nominalFlow * 1000.0 / 60.0 }
    val bottomAxisValueFormatter = remember(flowLmin) {
        CartesianValueFormatter { _, x, _ ->
            val flow = flowLmin * x.toDouble() / 100.0
            formatFlow(flow)
        }
    }
    val startAxisValueFormatter = remember {
        CartesianValueFormatter { _, y, _ ->
            String.format(Locale.US, "%.1f", y.toDouble())
        }
    }
    val customItemPlacer = remember {
        object : HorizontalAxis.ItemPlacer {
            private val tickValues = listOf(0.0, 50.0, 100.0, 140.0, 200.0)
            override fun getShiftExtremeLines(context: CartesianDrawingContext) = true
            override fun getFirstLabelValue(context: CartesianMeasuringContext, maxLabelWidth: Float) = tickValues.first()
            override fun getLastLabelValue(context: CartesianMeasuringContext, maxLabelWidth: Float) = tickValues.last()
            override fun getLabelValues(context: CartesianDrawingContext, visibleXRange: ClosedFloatingPointRange<Double>, fullXRange: ClosedFloatingPointRange<Double>, maxLabelWidth: Float) = tickValues
            override fun getWidthMeasurementLabelValues(context: CartesianMeasuringContext, layerDimensions: CartesianLayerDimensions, fullXRange: ClosedFloatingPointRange<Double>) = tickValues
            override fun getHeightMeasurementLabelValues(context: CartesianMeasuringContext, layerDimensions: CartesianLayerDimensions, fullXRange: ClosedFloatingPointRange<Double>, maxLabelWidth: Float) = tickValues
            override fun getLineValues(context: CartesianDrawingContext, visibleXRange: ClosedFloatingPointRange<Double>, fullXRange: ClosedFloatingPointRange<Double>, maxLabelWidth: Float) = tickValues
            override fun getStartLayerMargin(context: CartesianMeasuringContext, layerDimensions: CartesianLayerDimensions, tickThickness: Float, maxLabelWidth: Float) = 0f
            override fun getEndLayerMargin(context: CartesianMeasuringContext, layerDimensions: CartesianLayerDimensions, tickThickness: Float, maxLabelWidth: Float) = 0f
        }
    }

    LaunchedEffect(chartPoints) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = chartPoints.map { it.pct.toFloat() },
                    y = chartPoints.map { it.pressure!! }
                )
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            CartesianChartHost(
                chart = rememberCartesianChart(
                    rememberLineCartesianLayer(
                        rangeProvider = CartesianLayerRangeProvider.fixed(
                            minX = 0.0,
                            maxX = 200.0,
                        )
                    ),
                    startAxis = VerticalAxis.rememberStart(
                        valueFormatter = startAxisValueFormatter
                    ),
                    bottomAxis = HorizontalAxis.rememberBottom(
                        valueFormatter = bottomAxisValueFormatter,
                        itemPlacer = customItemPlacer
                    ),
                    getXStep = { _ -> 50.0 },
                ),
                modelProducer = modelProducer,
                scrollState = rememberVicoScrollState(scrollEnabled = false),
                zoomState = rememberVicoZoomState(zoomEnabled = false),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
            )

            if (curveData.isNotEmpty()) {
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                CurveDataTable(curveData = curveData)
            }
        }
    }
}

@Composable
private fun CurveDataTable(curveData: List<CurveChartPoint>) {
    val cellHeight = 48.dp
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val border = BorderStroke(0.5.dp, borderColor)
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val valueStyle = MaterialTheme.typography.bodySmall

    Column(modifier = Modifier.fillMaxWidth().border(border)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .height(cellHeight)
                    .border(border)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("", style = labelStyle, textAlign = TextAlign.Center)
            }
            curveData.forEach { point ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(cellHeight)
                        .border(border)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${point.pct}%",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .height(cellHeight)
                    .border(border)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.curve_chart_header_flow), style = labelStyle, textAlign = TextAlign.Center, maxLines = 2)
            }
            curveData.forEach { point ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(cellHeight)
                        .border(border)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(formatFlow(point.flow), style = valueStyle, textAlign = TextAlign.Center)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .weight(1.5f)
                    .height(cellHeight)
                    .border(border)
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.curve_chart_header_pressure), style = labelStyle, textAlign = TextAlign.Center, maxLines = 2)
            }
            curveData.forEach { point ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(cellHeight)
                        .border(border)
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        if (point.pressure != null) formatPressure(point.pressure) else "—",
                        style = valueStyle,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
