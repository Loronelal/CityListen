package com.citylisten.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.citylisten.presentation.R

internal enum class TopLevelDestination(
    val route: Any,
    val icon: ImageVector,
    val labelRes: Int
) {
    WALK(
        route = WalkRoute,
        icon = Icons.Default.PlayArrow,
        labelRes = R.string.nav_walk
    ),
    HISTORY(
        route = HistoryRoute,
        icon = Icons.AutoMirrored.Filled.List,
        labelRes = R.string.nav_history
    ),
    MAP(
        route = MapRoute,
        icon = Icons.Default.Place,
        labelRes = R.string.nav_map
    ),
    SETTINGS(
        route = SettingsRoute,
        icon = Icons.Default.Settings,
        labelRes = R.string.nav_settings
    )
}
