package com.igrupos.common.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import com.igrupos.common.R

enum class BottomNavItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    CLIENTS(route = "clients", labelRes = R.string.bottom_nav_clients, selectedIcon = Icons.Filled.People, unselectedIcon = Icons.Outlined.People),
    HISTORY(route = "history", labelRes = R.string.bottom_nav_history, selectedIcon = Icons.Filled.History, unselectedIcon = Icons.Outlined.History),
    CONVERTERS(route = "converters", labelRes = R.string.bottom_nav_converters, selectedIcon = Icons.Filled.Calculate, unselectedIcon = Icons.Outlined.Calculate),
    SETTINGS(route = "settings", labelRes = R.string.bottom_nav_settings, selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings)
}

val BOTTOM_NAV_ROUTES = BottomNavItem.entries.map { it.route }.toSet()

@Composable
fun IGruposBottomBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = stringResource(item.labelRes)
                    )
                },
                label = { Text(stringResource(item.labelRes)) }
            )
        }
    }
}
