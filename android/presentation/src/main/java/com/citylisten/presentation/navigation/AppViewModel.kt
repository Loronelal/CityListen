package com.citylisten.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citylisten.domain.model.ThemeMode
import com.citylisten.domain.usecase.ObserveOnboardingCompletedUseCase
import com.citylisten.domain.usecase.ObserveThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    observeOnboardingCompleted: ObserveOnboardingCompletedUseCase,
    observeThemeMode: ObserveThemeModeUseCase
) : ViewModel() {

    val onboardingCompleted: StateFlow<Boolean?> = observeOnboardingCompleted()
        .catch { emit(false) }
        .map<Boolean, Boolean?> { it }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    val themeMode: StateFlow<ThemeMode> = observeThemeMode()
        .catch { emit(ThemeMode.SYSTEM) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = ThemeMode.SYSTEM
        )

    companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
