package com.citylisten.presentation.navigation

import androidx.compose.foundation.layout.padding
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
import com.citylisten.presentation.history.HistoryRoute as HistoryScreenRoute
import com.citylisten.presentation.map.MapRoute as MapScreenRoute
import com.citylisten.presentation.onboarding.OnboardingRoute as OnboardingScreenRoute
import com.citylisten.presentation.permission.PermissionDeniedRoute as PermissionDeniedScreenRoute
import com.citylisten.presentation.permission.PermissionRoute as PermissionScreenRoute
import com.citylisten.presentation.permission.hasLocationPermission
import com.citylisten.presentation.permission.openAppSettings
import com.citylisten.presentation.profile.ProfileRoute as ProfileScreenRoute
import com.citylisten.presentation.settings.SettingsRoute as SettingsScreenRoute
import com.citylisten.presentation.story.StoryRoute as StoryScreenRoute
import com.citylisten.presentation.walk.WalkRoute as WalkScreenRoute
import kotlinx.coroutines.launch

@Composable
fun CityListenApp(
    startDestination: Any = WalkRoute,
    locationConsent: Boolean = false
) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val bottomBarVisible = currentDestination?.hierarchy?.any { destination ->
        TopLevelDestination.entries.any { destination.hasRoute(it.route::class) }
    } == true

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        bottomBar = {
            if (bottomBarVisible) {
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
        }
    ) { innerPadding ->
        CityListenNavHost(
            navController = navController,
            startDestination = startDestination,
            onStartWalk = {
                if (!locationConsent || !context.hasLocationPermission()) {
                    navController.navigate(PermissionRoute)
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            context.getString(R.string.walk_start_unavailable)
                        )
                    }
                }
            },
            onOpenSystemSettings = { context.openAppSettings() },
            modifier = Modifier.padding(innerPadding)
        )
    }
}

@Composable
internal fun CityListenNavHost(
    navController: NavHostController,
    startDestination: Any = WalkRoute,
    onStartWalk: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable<OnboardingRoute> {
            OnboardingScreenRoute(
                onStartWalk = {
                    navController.navigate(WalkRoute) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }
        composable<WalkRoute> {
            WalkScreenRoute(onStartWalk = onStartWalk)
        }
        composable<HistoryRoute> {
            HistoryScreenRoute(
                onBack = { navController.popBackStack() },
                onOpenStory = { storyId -> navController.navigate(StoryRoute(storyId)) },
                onStartWalk = {
                    navController.navigate(WalkRoute) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
        composable<MapRoute> {
            MapScreenRoute(
                onBack = { navController.popBackStack() },
                onOpenStory = { storyId -> navController.navigate(StoryRoute(storyId)) }
            )
        }
        composable<StoryRoute> {
            StoryScreenRoute(onBack = { navController.popBackStack() })
        }
        composable<SettingsRoute> {
            SettingsScreenRoute(
                onBack = { navController.popBackStack() },
                onOpenProfile = { navController.navigate(ProfileRoute) },
                onOpenPermission = { navController.navigate(PermissionRoute) },
                onOpenSystemSettings = onOpenSystemSettings
            )
        }
        composable<ProfileRoute> {
            ProfileScreenRoute(onBack = { navController.popBackStack() })
        }
        composable<PermissionRoute> {
            PermissionScreenRoute(
                onGranted = { navController.popBackStack() },
                onDenied = {
                    navController.navigate(PermissionDeniedRoute) {
                        popUpTo<PermissionRoute> { inclusive = true }
                    }
                },
                onSkip = { navController.popBackStack() }
            )
        }
        composable<PermissionDeniedRoute> {
            PermissionDeniedScreenRoute(
                onGranted = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
