
package com.igrupos.clientes

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igrupos.common.R
import com.igrupos.common.components.CollapsibleSection
import com.igrupos.common.components.ConfirmDeleteDialog
import com.igrupos.common.components.IGruposLoadingState
import com.igrupos.common.components.IGruposTopBar
import com.igrupos.common.components.StatusIndicator
import com.igrupos.core.util.DateUtils
import com.igrupos.domain.model.Client
import com.igrupos.domain.model.EntityStatus
import com.igrupos.domain.model.PressureGroup

@Composable
fun ClienteDetailScreen(
    clientId: Long,
    onGroupClick: (Long) -> Unit,
    onAddGroup: () -> Unit,
    onEditClient: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: ClienteDetailViewModel = hiltViewModel()
) {
    val client by viewModel.client.collectAsStateWithLifecycle()
    val groups by viewModel.groups.collectAsStateWithLifecycle()
    val installationAddress by viewModel.installationAddress.collectAsStateWithLifecycle()
    val isRefreshingAddress by viewModel.isRefreshingAddress.collectAsStateWithLifecycle()
    val groupStatuses by viewModel.groupStatuses.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            message = stringResource(R.string.delete_client_confirm),
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteClient { onBack() }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            IGruposTopBar(
                title = client?.name ?: stringResource(R.string.cliente_detail_title_fallback),
                onBack = onBack,
                actions = {
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_title),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { onEditClient(clientId) }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = stringResource(R.string.cliente_detail_edit_desc),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGroup,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cliente_detail_add_group_desc))
            }
        }
    ) { padding ->
        AnimatedContent(
            targetState = client,
            label = "client_detail",
            modifier = Modifier.fillMaxSize(),
            contentKey = { it != null },
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { currentClient ->
            if (currentClient == null) {
                IGruposLoadingState(modifier = Modifier.padding(padding))
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        CollapsibleSection(title = stringResource(R.string.cliente_detail_section_data)) {
                            ClientInfoCard(
                                client = currentClient,
                                installationAddress = installationAddress,
                                isRefreshingAddress = isRefreshingAddress,
                                onRefreshAddress = { viewModel.refreshInstallationAddress() }
                            )
                        }
                    }

                    item {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.cliente_detail_groups_title, groups.size),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(groups, key = { it.id }) { group ->
                        GroupCard(
                            group = group,
                            status = groupStatuses[group.id] ?: EntityStatus.GRAY,
                            onClick = { onGroupClick(group.id) },
                            modifier = Modifier.animateItem()
                        )
                    }

                    if (groups.isEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = stringResource(R.string.cliente_detail_no_groups),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ClientInfoCard(
    client: Client,
    installationAddress: String?,
    isRefreshingAddress: Boolean,
    onRefreshAddress: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = client.name,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            DetailRow(icon = Icons.Default.Business, label = stringResource(R.string.cliente_detail_label_cif), value = client.cif)
            DetailRow(icon = Icons.Default.LocationOn, label = stringResource(R.string.cliente_detail_label_address), value = client.address)
            DetailRow(icon = Icons.Default.LocationOn, label = stringResource(R.string.cliente_detail_label_province), value = client.province)
            DetailRow(icon = Icons.Default.Person, label = stringResource(R.string.cliente_detail_label_contact), value = client.contactPerson)
            DetailRow(icon = Icons.Default.Call, label = stringResource(R.string.cliente_detail_label_phone), value = client.phone)
            DetailRow(icon = Icons.Default.Email, label = stringResource(R.string.cliente_detail_label_email), value = client.email)

            if (client.latitude != null && client.longitude != null) {
                DetailRow(
                    icon = Icons.Default.LocationOn,
                    label = stringResource(R.string.cliente_detail_label_coordinates),
                    value = "${client.latitude}, ${client.longitude}"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.cliente_detail_installation_address),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
                if (isRefreshingAddress) {
                    CircularProgressIndicator(modifier = Modifier.height(16.dp).padding(end = 4.dp))
                } else {
                    IconButton(onClick = onRefreshAddress) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.cliente_detail_refresh_address_desc),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (installationAddress != null) {
                val openMapDesc = stringResource(R.string.cliente_detail_open_map_desc, installationAddress)
                Text(
                    text = installationAddress,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics { contentDescription = openMapDesc }
                        .clickable {
                            val uri = "geo:${client.latitude},${client.longitude}?q=${Uri.encode(installationAddress)}"
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
                            context.startActivity(intent)
                        }
                        .padding(vertical = 4.dp)
                )
            } else if (!isRefreshingAddress) {
                Text(
                    text = stringResource(R.string.cliente_detail_not_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            if (client.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.cliente_detail_notes_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = client.notes,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.label_created, DateUtils.formatDate(client.createdAt)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun GroupCard(
    group: PressureGroup,
    status: EntityStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groupCardDesc = stringResource(R.string.cliente_detail_group_card_desc, group.brand, group.model, group.serialNumber)
    Card(
        modifier = modifier
            .semantics { contentDescription = groupCardDesc }
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Shield,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = "${group.brand} ${group.model}",
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(R.string.label_serial, group.serialNumber),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusIndicator(status = status)
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    if (value.isNotBlank()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = 8.dp)
            )
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
