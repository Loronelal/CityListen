package com.citylisten.presentation.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citylisten.domain.model.ThemeMode
import com.citylisten.presentation.ui.theme.CityListenTheme

@Composable
fun CityListenRoot(viewModel: AppViewModel = hiltViewModel()) {
    val onboardingCompleted by viewModel.onboardingCompleted.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val locationConsent by viewModel.locationConsent.collectAsStateWithLifecycle()

    CityListenTheme(darkTheme = themeMode.isDarkTheme()) {
        if (onboardingCompleted == null) {
            LoadingScreen()
        } else {
            // Фиксируем стартовый экран на время жизни графа: смена startDestination
            // пересоздаёт NavHost и может сбросить текущий back stack.
            val startDestination = remember {
                if (onboardingCompleted == true) WalkRoute else OnboardingRoute
            }
            CityListenApp(
                startDestination = startDestination,
                locationConsent = locationConsent
            )
        }
    }
}

@Composable
private fun ThemeMode.isDarkTheme(): Boolean = when (this) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
