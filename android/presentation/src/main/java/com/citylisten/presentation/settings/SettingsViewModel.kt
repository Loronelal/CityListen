package com.citylisten.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citylisten.domain.model.ThemeMode
import com.citylisten.domain.usecase.ChangeLanguageUseCase
import com.citylisten.domain.usecase.ObserveLanguageUseCase
import com.citylisten.domain.usecase.ObserveLocationConsentUseCase
import com.citylisten.domain.usecase.ObserveProfileUseCase
import com.citylisten.domain.usecase.ObserveThemeModeUseCase
import com.citylisten.domain.usecase.SetThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeLanguage: ObserveLanguageUseCase,
    observeThemeMode: ObserveThemeModeUseCase,
    observeLocationConsent: ObserveLocationConsentUseCase,
    observeProfile: ObserveProfileUseCase,
    private val changeLanguage: ChangeLanguageUseCase,
    private val updateThemeMode: SetThemeModeUseCase
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        observeLanguage(),
        observeThemeMode(),
        observeLocationConsent(),
        observeProfile()
    ) { language, themeMode, locationConsent, profileName ->
        SettingsUiState(
            isLoading = false,
            language = language,
            themeMode = themeMode,
            locationConsent = locationConsent,
            profileName = profileName
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = SettingsUiState()
    )

    fun setLanguage(language: String) {
        viewModelScope.launch {
            changeLanguage(language)
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            updateThemeMode(mode)
        }
    }

    companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
