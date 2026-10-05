package com.fireflow.grupos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fireflow.common.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fireflow.common.components.FireFlowTopBar
import com.fireflow.domain.model.MotorType

@Composable
fun MotorFormScreen(
    clientId: Long,
    groupId: Long,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: MotorFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Scaffold(
        topBar = {
            FireFlowTopBar(title = stringResource(R.string.motor_form_title), onBack = onBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (state.availableYears.isNotEmpty() || state.motors.isNotEmpty()) {
                YearTabs(
                    availableYears = state.availableYears,
                    selectedYear = state.selectedYear,
                    onYearSelected = viewModel::onYearSelected,
                    onNewYear = viewModel::onNewYear
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            HorizontalDivider()
            Text(
                text = stringResource(R.string.motor_form_section_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            state.motors.forEachIndexed { index, entry ->
                MotorCard(
                    index = index,
                    entry = entry,
                    canRemove = state.motors.size > 1,
                    onTypeChange = { viewModel.onMotorTypeChange(index, it) },
                    onNominalFlowChange = { viewModel.onMotorNominalFlowChange(index, it) },
                    onManometricHeightChange = { viewModel.onMotorManometricHeightChange(index, it) },
                    onRemove = { viewModel.removeMotor(index) }
                )
            }

            OutlinedButton(
                onClick = { viewModel.addMotor() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text(stringResource(R.string.motor_form_add_button), modifier = Modifier.padding(start = 8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { viewModel.save() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !state.isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Text(
                    text = stringResource(R.string.motor_form_save_button),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            if (state.error != null) {
                Text(
                    text = state.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun YearTabs(
    availableYears: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit,
    onNewYear: () -> Unit
) {
    val allTabs = availableYears + listOf(-1)
    val selectedIndex = allTabs.indexOf(selectedYear).coerceAtLeast(0)

    Column {
        Text(
            text = stringResource(R.string.motor_form_year_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))

        TabRow(selectedTabIndex = selectedIndex.coerceIn(0, allTabs.lastIndex)) {
            allTabs.forEachIndexed { index, year ->
                Tab(
                    selected = year == selectedYear,
                    onClick = {
                        if (year == -1) {
                            onNewYear()
                        } else {
                            onYearSelected(year)
                        }
                    },
                    text = {
                        Text(
                            text = if (year == -1) stringResource(R.string.motor_form_new_year) else year.toString(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun MotorCard(
    index: Int,
    entry: MotorFormEntry,
    canRemove: Boolean,
    onTypeChange: (MotorType) -> Unit,
    onNominalFlowChange: (String) -> Unit,
    onManometricHeightChange: (String) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.motor_form_card_title, index + 1),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                if (canRemove) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.motor_form_delete_desc))
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = entry.motorType == MotorType.ELECTRIC,
                    onClick = { onTypeChange(MotorType.ELECTRIC) },
                    label = { Text(stringResource(R.string.motor_form_type_electric)) }
                )
                FilterChip(
                    selected = entry.motorType == MotorType.DIESEL,
                    onClick = { onTypeChange(MotorType.DIESEL) },
                    label = { Text(stringResource(R.string.motor_form_type_diesel)) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = entry.nominalFlow,
                onValueChange = onNominalFlowChange,
                label = { Text(stringResource(R.string.motor_form_flow_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )

            OutlinedTextField(
                value = entry.manometricHeight,
                onValueChange = onManometricHeightChange,
                label = { Text(stringResource(R.string.motor_form_height_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }
    }
}
