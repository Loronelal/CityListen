package com.citylisten.domain.usecase

import com.citylisten.domain.repository.SettingsRepository
import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult

class ChangeLanguageUseCase(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(language: String): DomainResult<Unit> {
        val normalized = language.trim().lowercase()
        if (normalized !in SUPPORTED) {
            return DomainResult.Failure(
                DomainError(DomainErrorType.INVALID_INPUT, "Неподдерживаемый язык: $language")
            )
        }
        settingsRepository.setLanguage(normalized)
        return DomainResult.Success(Unit)
    }

    companion object {
        val SUPPORTED = setOf("ru", "en")
    }
}