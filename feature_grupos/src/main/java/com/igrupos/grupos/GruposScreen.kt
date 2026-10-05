package com.igrupos.grupos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.igrupos.common.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igrupos.common.components.IGruposEmptyState
import com.igrupos.common.components.IGruposTopBar
import com.igrupos.common.components.StatusIndicator
import com.igrupos.domain.model.EntityStatus
import com.igrupos.domain.model.MotorType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GruposScreen(
    clientId: Long,
    onGroupClick: (Long) -> Unit,
    onAddGroup: () -> Unit,
    onBack: () -> Unit,
    viewModel: GruposViewModel = hiltViewModel()
) {
    val groupItems by viewModel.groupItemsWithStatus.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            IGruposTopBar(title = stringResource(R.string.grupos_title), onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddGroup,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.grupos_add_content_desc))
            }
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (groupItems.isEmpty()) {
                IGruposEmptyState(
                    icon = Icons.Default.Shield,
                    title = stringResource(R.string.grupos_empty_title),
                    subtitle = stringResource(R.string.grupos_empty_subtitle)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(groupItems, key = { it.group.id }) { item ->
                        GroupItem(
                            group = item.group,
                            motorTypes = item.motorTypes,
                            status = item.status,
                            onClick = { onGroupClick(item.group.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun GroupItem(
    group: com.igrupos.domain.model.PressureGroup,
    motorTypes: List<MotorType>,
    status: EntityStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val groupCardDesc = stringResource(R.string.grupos_card_content_desc, group.brand, group.model, group.serialNumber)
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
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(R.string.grupos_card_serial_power, group.serialNumber, group.power),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (motorTypes.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        motorTypes.forEach { type ->
                            FilterChip(
                                selected = false,
                                onClick = {},
                                label = {
                                    Text(
                                        text = stringResource(if (type == MotorType.ELECTRIC) R.string.grupos_motor_type_electric else R.string.grupos_motor_type_diesel),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                enabled = false
                            )
                        }
                    }
                }
            }
            StatusIndicator(status = status)
        }
    }
}
