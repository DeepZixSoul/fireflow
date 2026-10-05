package com.igrupos.clientes

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igrupos.common.R
import com.igrupos.common.components.IGruposTopBar

@Composable
fun ClienteFormScreen(
    clientId: Long?,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: ClienteFormViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEditing = clientId != null

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) onSaved()
    }

    Scaffold(
        topBar = {

            IGruposTopBar(
                title = stringResource(if (isEditing) R.string.cliente_form_edit_title else R.string.cliente_form_new_title),
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
                value = state.name,
                onValueChange = { viewModel.onNameChange(it) },
                label = { Text(stringResource(R.string.cliente_form_name_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.nameError != null,
                supportingText = state.nameError?.let { { Text(stringResource(it)) } }
            )

            OutlinedTextField(
                value = state.cif,
                onValueChange = { viewModel.onCifChange(it) },
                label = { Text(stringResource(R.string.cliente_form_cif_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = state.cifError != null,
                supportingText = state.cifError?.let { { Text(stringResource(it)) } }
            )

            OutlinedTextField(
                value = state.address,
                onValueChange = { viewModel.onAddressChange(it) },
                label = { Text(stringResource(R.string.cliente_form_address_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.province,
                onValueChange = { viewModel.onProvinceChange(it) },
                label = { Text(stringResource(R.string.cliente_form_province_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            LocationCard(state = state)

            OutlinedTextField(
                value = state.contactPerson,
                onValueChange = { viewModel.onContactPersonChange(it) },
                label = { Text(stringResource(R.string.cliente_form_contact_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.phone,
                onValueChange = { viewModel.onPhoneChange(it) },
                label = { Text(stringResource(R.string.cliente_form_phone_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.email,
                onValueChange = { viewModel.onEmailChange(it) },
                label = { Text(stringResource(R.string.cliente_form_email_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.notes,
                onValueChange = { viewModel.onNotesChange(it) },
                label = { Text(stringResource(R.string.cliente_form_notes_label)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                minLines = 3,
                maxLines = 5
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
                    text = stringResource(if (isEditing) R.string.cliente_form_save_edit else R.string.cliente_form_save_new),
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            state.error?.let {
                Text(
                    text = stringResource(it),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun LocationCard(state: ClienteFormState) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        if (state.isGeocoding) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp).width(20.dp))
                Text(
                    text = stringResource(R.string.cliente_form_geocoding),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (state.latitude != null && state.longitude != null) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.cliente_form_location_title),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                Text(
                    text = "${"%.6f".format(state.latitude)}, ${"%.6f".format(state.longitude)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Button(
                    onClick = {
                        val uri = "geo:${state.latitude},${state.longitude}?q=${Uri.encode("${state.address}, ${state.province}")}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                        context.startActivity(intent)
                    },
                    modifier = Modifier.padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Text(stringResource(R.string.cliente_form_view_map), modifier = Modifier.padding(start = 4.dp))
                }
            }
        } else if (state.geocodeError != null) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = state.geocodeError?.let { stringResource(it) } ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}
