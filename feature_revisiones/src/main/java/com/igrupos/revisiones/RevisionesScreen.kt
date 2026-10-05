package com.igrupos.revisiones

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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.res.stringResource
import com.igrupos.common.R
import com.igrupos.common.components.IGruposEmptyState
import com.igrupos.common.components.IGruposTopBar
import com.igrupos.core.util.DateUtils
import com.igrupos.domain.model.Revision

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RevisionesScreen(
    clientId: Long,
    groupId: Long,
    onRevisionClick: (Long) -> Unit,
    onAddRevision: () -> Unit,
    onBack: () -> Unit,
    viewModel: RevisionesViewModel = hiltViewModel()
) {
    val revisions by viewModel.revisions.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            IGruposTopBar(title = stringResource(R.string.revisiones_title), onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddRevision,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.revisiones_add_desc))
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
            if (revisions.isEmpty()) {
                IGruposEmptyState(
                    icon = Icons.Default.Assignment,
                    title = stringResource(R.string.revisiones_empty_title),
                    subtitle = stringResource(R.string.revisiones_empty_subtitle)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(revisions, key = { it.id }) { revision ->
                        RevisionCard(
                            revision = revision,
                            onClick = { onRevisionClick(revision.id) },
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RevisionCard(
    revision: Revision,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val revisionCardDesc = stringResource(R.string.label_revision_card, revision.technicianName, DateUtils.formatDateTime(revision.date))
    Card(
        modifier = modifier
            .semantics { contentDescription = revisionCardDesc }
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
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(
                    text = DateUtils.formatDateTime(revision.date),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = stringResource(R.string.label_technician, revision.technicianName),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (revision.notes.isNotBlank()) {
                    Text(
                        text = revision.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
