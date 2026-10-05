package com.igrupos.grupos.components

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.igrupos.common.R
import com.igrupos.domain.model.Motor
import com.igrupos.domain.model.PressureMeasurement

@Composable
fun PressureMeasurementsSection(
    motors: List<Motor>,
    measurements: Map<Long, PressureMeasurement?>,
    year: Int,
    onCreateNextYear: (Long) -> Unit,
    onPressureChange: (motorId: Long, field: String, value: String) -> Unit
) {
    Column {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.pressure_table_title, year),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))

        motors.forEachIndexed { index, motor ->
            val measurement = measurements[motor.id]
            val flowLmin = motor.nominalFlow * 1000.0 / 60.0
            val percentages = listOf(0, 50, 100, 140)
            val flowValues = percentages.map { pct ->
                (flowLmin * pct / 100.0).toBigDecimal().stripTrailingZeros().toPlainString()
            }
            val pressureValues = listOf(
                measurement?.pressureAt0,
                measurement?.pressureAt50,
                measurement?.pressureAt100,
                measurement?.pressureAt140
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val typeLabelRes = if (motor.motorType.name == "ELECTRIC") R.string.motor_form_type_electric else R.string.motor_form_type_diesel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.pressure_motor_card_title, index + 1, stringResource(typeLabelRes)),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onCreateNextYear(motor.id) }) {
                            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.pressure_add_year_desc))
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    val fieldKeys = listOf("0", "50", "100", "140")
                    PressureTable(
                        flowValues = flowValues,
                        pressureValues = pressureValues.map { it?.toBigDecimal()?.stripTrailingZeros()?.toPlainString() ?: "" },
                        nominalFlow = motor.nominalFlow.toBigDecimal().stripTrailingZeros().toPlainString(),
                        manometricHeight = motor.manometricHeight.toBigDecimal().stripTrailingZeros().toPlainString(),
                        onPressureChange = { index, value ->
                            onPressureChange(motor.id, fieldKeys[index], value)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
private fun PressureTable(
    flowValues: List<String>,
    pressureValues: List<String>,
    nominalFlow: String,
    manometricHeight: String,
    onPressureChange: (index: Int, value: String) -> Unit
) {
    val cellHeight = 48.dp
    val borderColor = MaterialTheme.colorScheme.outlineVariant
    val border = BorderStroke(0.5.dp, borderColor)
    val labelStyle = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    val valueStyle = MaterialTheme.typography.bodySmall
    val percentages = listOf(0, 50, 100, 140)
    val editingValues = remember { mutableStateMapOf<Int, String>() }

    Column(modifier = Modifier.fillMaxWidth().border(border)) {
        Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant)) {
            Box(
                modifier = Modifier.weight(1.5f).height(cellHeight).border(border).padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("$nominalFlow/$manometricHeight", style = labelStyle, textAlign = TextAlign.Center)
            }
            percentages.forEach { pct ->
                Box(
                    modifier = Modifier.weight(1f).height(cellHeight).border(border).padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "${pct}%",
                        style = MaterialTheme.typography.labelMedium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.weight(1.5f).height(cellHeight).border(border).padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.pressure_table_header_flow), style = labelStyle, textAlign = TextAlign.Center, maxLines = 2)
            }
            flowValues.forEach { value ->
                Box(
                    modifier = Modifier.weight(1f).height(cellHeight).border(border).padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(value, style = valueStyle, textAlign = TextAlign.Center)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier.weight(1.5f).height(cellHeight).border(border).padding(horizontal = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.pressure_table_header_pressure), style = labelStyle, textAlign = TextAlign.Center, maxLines = 2)
            }
            pressureValues.forEachIndexed { i, value ->
                val displayValue = editingValues[i] ?: value
                OutlinedTextField(
                    value = displayValue,
                    onValueChange = { input ->
                        val filtered = input.filter { it.isDigit() || it == '.' || it == ',' }
                        val dotCount = filtered.count { it == '.' }
                        val commaCount = filtered.count { it == ',' }
                        if (dotCount + commaCount <= 1) {
                            editingValues[i] = filtered
                            onPressureChange(i, filtered)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(cellHeight)
                        .border(border)
                        .padding(2.dp)
                        .onFocusChanged { focusState ->
                            if (!focusState.isFocused) {
                                editingValues.remove(i)
                            }
                        },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        capitalization = KeyboardCapitalization.None
                    ),
                    textStyle = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}
