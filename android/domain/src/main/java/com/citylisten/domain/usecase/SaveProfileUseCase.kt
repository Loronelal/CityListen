package com.citylisten.domain.usecase

import com.citylisten.domain.repository.SettingsRepository
import com.citylisten.domain.result.DomainResult
import com.citylisten.domain.validation.DisplayNameValidator
import javax.inject.Inject

class SaveProfileUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val validator: DisplayNameValidator
) {
    suspend operator fun invoke(rawName: String): DomainResult<String> {
        return when (val validation = validator.validate(rawName)) {
            is DomainResult.Success -> {
                settingsRepository.setProfileName(validation.data)
                DomainResult.Success(validation.data)
            }
            is DomainResult.Failure -> validation
        }
    }
}
