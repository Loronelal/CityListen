package com.citylisten.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.citylisten.presentation.R
import com.citylisten.presentation.walk.WalkRoute as WalkScreenRoute
import kotlinx.coroutines.launch

@Composable
fun CityListenApp() {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                TopLevelDestination.entries.forEach { destination ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(destination.route::class)
                        } == true,
                        onClick = {
                            navController.navigate(destination.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = destination.icon,
                                contentDescription = null
                            )
                        },
                        label = { Text(text = stringResource(destination.labelRes)) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        CityListenNavHost(
            navController = navController,
            onStartWalk = {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.walk_start_unavailable)
                    )
                }
            },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
internal fun CityListenNavHost(
    navController: NavHostController,
    onStartWalk: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = WalkRoute,
        modifier = modifier
    ) {
        composable<WalkRoute> {
            WalkScreenRoute(onStartWalk = onStartWalk)
        }
        composable<HistoryRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.history_title),
                message = stringResource(R.string.history_placeholder),
                icon = Icons.AutoMirrored.Filled.List
            )
        }
        composable<MapRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.map_title),
                message = stringResource(R.string.map_placeholder),
                icon = Icons.Default.Place
            )
        }
        composable<SettingsRoute> {
            PlaceholderScreen(
                title = stringResource(R.string.settings_title),
                message = stringResource(R.string.settings_placeholder),
                icon = Icons.Default.Settings
            )
        }
    }
}
