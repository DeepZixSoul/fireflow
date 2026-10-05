package com.igrupos.conversiones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import com.igrupos.common.components.IGruposTopBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.igrupos.common.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ConverterScreen(
    onBack: () -> Unit,
    viewModel: ConverterViewModel = hiltViewModel()
) {
    val values by viewModel.values.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            IGruposTopBar(title = stringResource(R.string.converter_title), showBack = false)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.converter_flow_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ConverterField(
                        label = stringResource(R.string.converter_unit_lmin),
                        value = values.lmin,
                        onValueChange = { viewModel.updateValue(ConverterUnit.LMIN, it) }
                    )
                    ConverterField(
                        label = stringResource(R.string.converter_unit_m3h),
                        value = values.m3h,
                        onValueChange = { viewModel.updateValue(ConverterUnit.M3H, it) }
                    )
                    ConverterField(
                        label = stringResource(R.string.converter_unit_gpm),
                        value = values.gpm,
                        onValueChange = { viewModel.updateValue(ConverterUnit.GPM, it) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.converter_pressure_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ConverterField(
                        label = stringResource(R.string.converter_unit_psi),
                        value = values.psi,
                        onValueChange = { viewModel.updateValue(ConverterUnit.PSI, it) }
                    )
                    ConverterField(
                        label = stringResource(R.string.converter_unit_bar),
                        value = values.bar,
                        onValueChange = { viewModel.updateValue(ConverterUnit.BAR, it) }
                    )
                    ConverterField(
                        label = stringResource(R.string.converter_unit_kpa),
                        value = values.kpa,
                        onValueChange = { viewModel.updateValue(ConverterUnit.KPA, it) }
                    )
                    ConverterField(
                        label = stringResource(R.string.converter_unit_mca),
                        value = values.mca,
                        onValueChange = { viewModel.updateValue(ConverterUnit.MCA, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConverterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
    )
}
