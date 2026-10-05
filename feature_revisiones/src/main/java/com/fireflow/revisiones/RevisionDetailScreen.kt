package com.fireflow.revisiones

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.annotation.StringRes
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fireflow.common.R
import com.fireflow.common.components.ConfirmDeleteDialog
import com.fireflow.common.components.FireFlowLoadingState
import com.fireflow.common.components.FireFlowTopBar
import com.fireflow.core.util.DateUtils

@Composable
fun RevisionDetailScreen(
    revisionId: Long,
    onCurvasClick: (Long) -> Unit,
    onPhotosClick: (Long) -> Unit,
    onReportClick: (Long) -> Unit,
    onExportClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: RevisionDetailViewModel = hiltViewModel()
) {
    val revision by viewModel.revision.collectAsStateWithLifecycle()
    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        ConfirmDeleteDialog(
            message = stringResource(R.string.delete_revision_confirm),
            onConfirm = {
                showDeleteDialog = false
                viewModel.deleteRevision { onBack() }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    Scaffold(
        topBar = {
            FireFlowTopBar(
                title = stringResource(R.string.revision_detail_title),
                onBack = onBack,
                actions = {
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
            targetState = revision,
            label = "revision_detail",
            modifier = Modifier.fillMaxSize(),
            contentKey = { it != null },
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { currentRevision ->
            if (currentRevision == null) {
                FireFlowLoadingState(modifier = Modifier.padding(padding))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = DateUtils.formatDateTime(currentRevision.date),
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.label_technician, currentRevision.technicianName),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (currentRevision.notes.isNotBlank()) {
                        Text(
                            text = stringResource(R.string.revision_detail_notes_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = currentRevision.notes,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    if (currentRevision.checklistResults.isNotEmpty()) {
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.revision_detail_checklist_title),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        currentRevision.checklistResults.forEach { (label, checked) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(checked = checked, onCheckedChange = null)
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = stringResource(R.string.revision_detail_actions_title),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ActionButton(
                        icon = Icons.Default.BarChart,
                        labelRes = R.string.revision_detail_action_curves,
                        onClick = { onCurvasClick(revisionId) }
                    )

                    ActionButton(
                        icon = Icons.Default.CameraAlt,
                        labelRes = R.string.revision_detail_action_photos,
                        onClick = { onPhotosClick(revisionId) }
                    )

                    ActionButton(
                        icon = Icons.Default.Description,
                        labelRes = R.string.revision_detail_action_report,
                        onClick = { onReportClick(revisionId) }
                    )

                    ActionButton(
                        icon = Icons.Default.FileDownload,
                        labelRes = R.string.revision_detail_action_export,
                        onClick = { onExportClick(revisionId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    @StringRes labelRes: Int,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Icon(imageVector = icon, contentDescription = null)
        Text(
            text = stringResource(labelRes),
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}
