package com.citylisten.presentation.settings

import com.citylisten.domain.model.ThemeMode

data class SettingsUiState(
    val isLoading: Boolean = true,
    val language: String = "ru",
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val locationConsent: Boolean = false,
    val profileName: String = ""
)
