package com.citylisten.domain.usecase

import com.citylisten.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveLanguageUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<String> = settingsRepository.observeLanguage()
}
