package com.citylisten.domain.usecase

import com.citylisten.domain.repository.SettingsRepository
import javax.inject.Inject

class CompleteOnboardingUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke() {
        settingsRepository.setOnboardingCompleted(true)
    }
}
