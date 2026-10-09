package com.fireflow.configuracion

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.fireflow.common.R

@Composable
internal fun SecurityCard(uiState: SettingsUiState, viewModel: SettingsViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.settings_security_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            val onDesc = stringResource(R.string.settings_flag_secure_on)
            val offDesc = stringResource(R.string.settings_flag_secure_off)

            SettingRow(
                icon = Icons.Default.Shield,
                title = stringResource(R.string.settings_flag_secure_title),
                description = if (uiState.flagSecureEnabled) onDesc else offDesc,
                trailing = {
                    Switch(
                        checked = uiState.flagSecureEnabled,
                        onCheckedChange = { viewModel.toggleFlagSecure() },
                        modifier = Modifier.semantics {
                            stateDescription = if (uiState.flagSecureEnabled) onDesc else offDesc
                        }
                    )
                }
            )
        }
    }
}
