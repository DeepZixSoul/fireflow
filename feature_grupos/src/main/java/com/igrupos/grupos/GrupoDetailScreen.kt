package com.igrupos.grupos

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.igrupos.common.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igrupos.common.components.CollapsibleSection
import com.igrupos.common.components.ConfirmDeleteDialog
import com.igrupos.common.components.CurveChartSection
import com.igrupos.common.components.IGruposLoadingState
import com.igrupos.common.components.IGruposTopBar
import com.igrupos.common.components.StatusIndicator
import com.igrupos.common.components.YearTabs
import com.igrupos.core.util.DateUtils
import com.igrupos.domain.model.EntityStatus
import com.igrupos.domain.model.Revision
import com.igrupos.grupos.components.GroupInfoCard
import com.igrupos.grupos.components.PressureMeasurementsSection

@Composable
fun GrupoDetailScreen(
    clientId: Long,
    groupId: Long,
    onRevisionClick: (Long) -> Unit,
    onAddRevision: () -> Unit,
    onEditMotors: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: GrupoDetailViewModel = hiltViewModel()
) {
    val group by viewModel.group.collectAsStateWithLifecycle()
    val revisions by viewModel.revisions.collectAsStateWithLifecycle()
    val motors by viewModel.motors.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val isYearExpanded by viewModel.isYearExpanded.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val motorMeasurements by viewModel.motorMeasurements.collectAsStateWithLifecycle()
    val revisionStatuses by viewModel.revisionStatuses.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            message = stringResource(R.string.delete_group_confirm),
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteGroup { onBack() }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            IGruposTopBar(
                title = group?.let { "${it.brand} ${it.model}" } ?: stringResource(R.string.grupo_detail_title_fallback),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onAddRevision) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = stringResource(R.string.grupo_detail_add_revision_desc),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_title),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { padding ->
        AnimatedContent(
            targetState = group,
            label = "group_detail",
            modifier = Modifier.fillMaxSize(),
            contentKey = { it != null },
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { currentGroup ->
            if (currentGroup == null) {
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
                        CollapsibleSection(title = stringResource(R.string.grupo_detail_section_data)) {
                            GroupInfoCard(group = currentGroup)
                        }
                    }

                    if (availableYears.isNotEmpty() || motors.isNotEmpty()) {
                        item {
                            YearTabs(
                                availableYears = availableYears,
                                selectedYear = selectedYear,
                                onYearSelected = viewModel::onYearSelected
                            )
                        }
                    }

                    if (isYearExpanded) {
                        item {
                            OutlinedButton(
                                onClick = { onEditMotors(groupId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text(
                                    text = stringResource(R.string.grupo_detail_edit_motors),
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }

                        if (motors.isNotEmpty() && motorMeasurements.isNotEmpty()) {
                            item {
                                PressureMeasurementsSection(
                                    motors = motors,
                                    measurements = motorMeasurements,
                                    year = selectedYear,
                                    onCreateNextYear = viewModel::createNextYearMeasurement,
                                    onPressureChange = viewModel::onPressureChange
                                )
                            }
                        }

                        if (motors.isNotEmpty()) {
                            item {
                                CurveChartSection(
                                    motors = motors,
                                    measurements = motorMeasurements
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.grupo_detail_revisions_title, revisions.size),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    items(revisions, key = { it.id }) { revision ->
                        RevisionItem(
                            revision = revision,
                            status = revisionStatuses[revision.id] ?: EntityStatus.GRAY,
                            onClick = { onRevisionClick(revision.id) },
                            modifier = Modifier.animateItem()
                        )
                    }

                    if (revisions.isEmpty()) {
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                                Text(
                                    text = stringResource(R.string.grupo_detail_no_revisions),
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
private fun RevisionItem(
    revision: Revision,
    status: EntityStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val revisionCardDesc = stringResource(R.string.label_revision_card, revision.technicianName, DateUtils.formatDateTime(revision.date))
    androidx.compose.material3.Card(
        modifier = modifier
            .semantics { contentDescription = revisionCardDesc }
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(
                    text = DateUtils.formatDateTime(revision.date),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(R.string.label_technician, revision.technicianName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusIndicator(status = status)
        }
    }
}
