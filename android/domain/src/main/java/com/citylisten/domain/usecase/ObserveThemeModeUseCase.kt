package com.citylisten.domain.usecase

import com.citylisten.domain.model.ThemeMode
import com.citylisten.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveThemeModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<ThemeMode> = settingsRepository.observeThemeMode()
}
