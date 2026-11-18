package com.citylisten.presentation.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val ParkColorScheme = lightColorScheme(
    primary = ParkGreenPrimary,
    onPrimary = SurfaceLight,
    primaryContainer = ParkGreenContainer,
    onPrimaryContainer = OnBackgroundLight,
    secondary = SunYellowSecondary,
    onSecondary = OnSecondaryLight,
    tertiary = SkyBlueAccent,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnBackgroundLight,
    surfaceVariant = MapSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorRedLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = ErrorRedLight
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetOrangePrimary,
    onPrimary = OnPrimaryDark,
    primaryContainer = SunsetOrangeContainer,
    onPrimaryContainer = OnBackgroundDark,
    secondary = TwilightPurpleSecondary,
    onSecondary = OnPrimaryDark,
    tertiary = GoldAccent,
    onTertiary = OnPrimaryDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnBackgroundDark,
    surfaceVariant = MapSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorCoralDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorCoralDark
)

@Composable
fun CityListenTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> SunsetColorScheme
        else -> ParkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = CityListenTypography,
        content = content
    )
}
