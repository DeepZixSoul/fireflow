package com.igrupos.grupos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.igrupos.common.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igrupos.common.components.IGruposTopBar
import com.igrupos.core.util.DateUtils

@Composable
fun GrupoFormScreen(
    clientId: Long,
    groupId: Long?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: GrupoFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = groupId != null

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Scaffold(
        topBar = {
            IGruposTopBar(
                title = stringResource(if (isEditing) R.string.grupo_form_edit_title else R.string.grupo_form_new_title),
                onBack = onBack
            )
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
            OutlinedTextField(
                value = state.brand,
                onValueChange = { viewModel.onBrandChange(it) },
                label = { Text(stringResource(R.string.grupo_form_brand_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.model,
                onValueChange = { viewModel.onModelChange(it) },
                label = { Text(stringResource(R.string.grupo_form_model_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.serialNumber,
                onValueChange = { viewModel.onSerialNumberChange(it) },
                label = { Text(stringResource(R.string.grupo_form_serial_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.pumpNumber,
                onValueChange = { viewModel.onPumpNumberChange(it) },
                label = { Text(stringResource(R.string.grupo_form_pump_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.manufacturer,
                onValueChange = { viewModel.onManufacturerChange(it) },
                label = { Text(stringResource(R.string.grupo_form_manufacturer_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.power,
                onValueChange = { viewModel.onPowerChange(it) },
                label = { Text(stringResource(R.string.grupo_form_power_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            DateField(
                value = state.installationDate,
                onValueChange = { viewModel.onInstallationDateChange(it) },
                label = stringResource(R.string.grupo_form_installation_date)
            )

            DateField(
                value = state.maintenanceDate,
                onValueChange = { viewModel.onMaintenanceDateChange(it) },
                label = stringResource(R.string.grupo_form_maintenance_date)
            )

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
                    text = stringResource(if (isEditing) R.string.grupo_form_save_edit else R.string.grupo_form_save_new),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String
) {
    var showDialog by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = value.toLongOrNull() ?: System.currentTimeMillis()
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = if (value.isNotEmpty()) {
                try {
                    DateUtils.formatDate(value.toLong())
                } catch (_: Exception) {
                    value
                }
            } else "",
            onValueChange = {},
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            enabled = true
        )
        val dateSelectDesc = stringResource(R.string.grupo_form_date_select_desc)
        Box(
            modifier = Modifier
                .matchParentSize()
                .semantics { contentDescription = dateSelectDesc }
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onValueChange(millis.toString())
                        }
                        showDialog = false
                    }
                ) {
                    Text(stringResource(R.string.common_accept))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
