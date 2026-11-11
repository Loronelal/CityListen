package com.citylisten.ui.theme

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
    onSecondary = OnBackgroundLight,
    tertiary = SkyBlueAccent,
    background = BackgroundLight,
    onBackground = OnBackgroundLight,
    surface = SurfaceLight,
    onSurface = OnBackgroundLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    error = ErrorRedLight
)

private val SunsetColorScheme = darkColorScheme(
    primary = SunsetOrangePrimary,
    onPrimary = OnBackgroundDark,
    primaryContainer = SunsetOrangeContainer,
    onPrimaryContainer = OnBackgroundDark,
    secondary = TwilightPurpleSecondary,
    onSecondary = OnBackgroundDark,
    tertiary = GoldAccent,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnBackgroundDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    error = ErrorCoralDark
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