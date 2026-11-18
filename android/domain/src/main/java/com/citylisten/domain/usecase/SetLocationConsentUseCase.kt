package com.citylisten.domain.usecase

import com.citylisten.domain.repository.SettingsRepository
import javax.inject.Inject

class SetLocationConsentUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(consent: Boolean) {
        settingsRepository.setLocationConsent(consent)
    }
}
