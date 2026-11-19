package com.citylisten.presentation.settings

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.citylisten.domain.model.ThemeMode
import com.citylisten.presentation.R
import com.citylisten.presentation.components.ScreenHeader
import com.citylisten.presentation.permission.hasLocationPermission
import com.citylisten.presentation.ui.theme.CityListenTheme

@Composable
fun SettingsRoute(
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onOpenPermission: () -> Unit,
    onOpenSystemSettings: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appVersion = remember(context) { context.appVersionName() }

    SettingsScreen(
        uiState = uiState,
        appVersion = appVersion,
        onBack = onBack,
        onOpenProfile = onOpenProfile,
        onLocationClick = {
            if (uiState.locationConsent && context.hasLocationPermission()) {
                onOpenSystemSettings()
            } else {
                onOpenPermission()
            }
        },
        onLanguageChange = viewModel::setLanguage,
        onThemeChange = viewModel::setThemeMode
    )
}

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    appVersion: String,
    onBack: () -> Unit,
    onOpenProfile: () -> Unit,
    onLocationClick: () -> Unit,
    onLanguageChange: (String) -> Unit,
    onThemeChange: (ThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ScreenHeader(
            title = stringResource(R.string.settings_title),
            onBack = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                SettingsSection(title = stringResource(R.string.settings_profile)) {
                    ProfileCard(
                        name = uiState.profileName.ifBlank {
                            stringResource(R.string.settings_guest)
                        },
                        onClick = onOpenProfile
                    )
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_language)) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        languageOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = uiState.language == option.code,
                                onClick = { onLanguageChange(option.code) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = languageOptions.size
                                )
                            ) {
                                Text(text = stringResource(option.labelRes))
                            }
                        }
                    }
                }
            }

            item {
                SettingsSection(title = stringResource(R.string.settings_theme)) {
                    SingleChoiceSegmentedButtonRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        themeOptions.forEachIndexed { index, option ->
                            SegmentedButton(
                                selected = uiState.themeMode == option.mode,
                                onClick = { onThemeChange(option.mode) },
                                shape = SegmentedButtonDefaults.itemShape(
                                    index = index,
                                    count = themeOptions.size
                                )
                            ) {
                                Text(text = stringResource(option.labelRes))
                            }
                        }
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    SettingsRow(
                        icon = Icons.Default.Place,
                        title = stringResource(R.string.settings_location),
                        subtitle = stringResource(
                            if (uiState.locationConsent) {
                                R.string.settings_location_on
                            } else {
                                R.string.settings_location_off
                            }
                        ),
                        onClick = onLocationClick
                    )
                }
            }

            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    SettingsRow(
                        icon = Icons.Default.Info,
                        title = stringResource(R.string.settings_about),
                        subtitle = stringResource(R.string.settings_version),
                        trailing = appVersion
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            content()
        }
    }
}

@Composable
private fun ProfileCard(
    name: String,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = stringResource(R.string.settings_profile_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: String? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            Text(
                text = trailing,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (onClick != null) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private data class LanguageOption(val code: String, val labelRes: Int)

private data class ThemeOption(val mode: ThemeMode, val labelRes: Int)

private val languageOptions = listOf(
    LanguageOption(code = "ru", labelRes = R.string.settings_language_ru),
    LanguageOption(code = "en", labelRes = R.string.settings_language_en)
)

private val themeOptions = listOf(
    ThemeOption(mode = ThemeMode.SYSTEM, labelRes = R.string.settings_theme_system),
    ThemeOption(mode = ThemeMode.LIGHT, labelRes = R.string.settings_theme_light),
    ThemeOption(mode = ThemeMode.DARK, labelRes = R.string.settings_theme_dark)
)

@Suppress("DEPRECATION")
private fun Context.appVersionName(): String {
    return try {
        packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
    } catch (exception: PackageManager.NameNotFoundException) {
        "1.0"
    }
}

@Preview(name = "Settings", showBackground = true, heightDp = 874)
@Composable
private fun SettingsPreview() {
    CityListenTheme {
        SettingsScreen(
            uiState = SettingsUiState(
                isLoading = false,
                language = "ru",
                themeMode = ThemeMode.SYSTEM,
                locationConsent = true,
                profileName = "Анна"
            ),
            appVersion = "1.0",
            onBack = {},
            onOpenProfile = {},
            onLocationClick = {},
            onLanguageChange = {},
            onThemeChange = {}
        )
    }
}
