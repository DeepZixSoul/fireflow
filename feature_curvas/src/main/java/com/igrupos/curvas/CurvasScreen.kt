package com.igrupos.curvas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.igrupos.common.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.igrupos.common.components.CurveChartSection
import com.igrupos.common.components.IGruposEmptyState
import com.igrupos.common.components.IGruposLoadingState
import com.igrupos.common.components.IGruposTopBar
import com.igrupos.common.components.YearTabs

@Composable
fun CurvasScreen(
    revisionId: Long,
    onBack: () -> Unit,
    viewModel: CurvasViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedYear by viewModel.selectedYear.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val measurements by viewModel.measurements.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            IGruposTopBar(title = stringResource(R.string.curvas_title), onBack = onBack)
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                IGruposLoadingState(modifier = Modifier.padding(padding))
            }

            uiState.motors.isEmpty() -> {
                IGruposEmptyState(
                    icon = Icons.Default.BarChart,
                    title = stringResource(R.string.curvas_empty_title),
                    subtitle = stringResource(R.string.curvas_empty_subtitle),
                    modifier = Modifier.padding(padding)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (availableYears.isNotEmpty()) {
                        item {
                            YearTabs(
                                availableYears = availableYears,
                                selectedYear = selectedYear,
                                onYearSelected = viewModel::onYearSelected
                            )
                        }
                    }

                    item {
                        CurveChartSection(
                            motors = uiState.motors,
                            measurements = measurements
                        )
                    }
                }
            }
        }
    }
}
