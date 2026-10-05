package com.igrupos.common.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.igrupos.common.R

@Composable
fun YearTabs(
    availableYears: List<Int>,
    selectedYear: Int,
    onYearSelected: (Int) -> Unit
) {
    Column {
        Text(
            text = stringResource(R.string.pressure_section_year_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))

        TabRow(selectedTabIndex = availableYears.indexOf(selectedYear).coerceAtLeast(0)) {
            availableYears.forEach { year ->
                Tab(
                    selected = year == selectedYear,
                    onClick = { onYearSelected(year) },
                    text = {
                        Text(
                            text = year.toString(),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                )
            }
        }
    }
}
