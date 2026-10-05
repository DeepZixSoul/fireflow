package com.fireflow.grupos.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.fireflow.common.R
import com.fireflow.core.util.DateUtils
import com.fireflow.domain.model.PressureGroup

@Composable
fun GroupInfoCard(group: PressureGroup) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            InfoRow(label = stringResource(R.string.group_info_brand), value = group.brand)
            InfoRow(label = stringResource(R.string.group_info_model), value = group.model)
            InfoRow(label = stringResource(R.string.group_info_serial), value = group.serialNumber)
            InfoRow(label = stringResource(R.string.group_info_pump), value = group.pumpNumber)
            InfoRow(label = stringResource(R.string.group_info_manufacturer), value = group.manufacturer)
            InfoRow(label = stringResource(R.string.group_info_power), value = group.power)

            if (group.installationDate != null) {
                InfoRow(
                    label = stringResource(R.string.group_info_installation_date),
                    value = DateUtils.formatDate(group.installationDate!!)
                )
            }

            if (group.maintenanceDate != null) {
                InfoRow(
                    label = stringResource(R.string.group_info_maintenance_date),
                    value = DateUtils.formatDate(group.maintenanceDate!!)
                )
            }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        ) {
            Text(
                text = "$label: ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
