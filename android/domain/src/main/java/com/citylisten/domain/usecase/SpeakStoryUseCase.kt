package com.citylisten.domain.usecase

import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.TtsRepository
import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult

class SpeakStoryUseCase(
    private val ttsRepository: TtsRepository,
    private val clock: () -> Long = System::currentTimeMillis
) {
    private var lastSpokenAt: Long = 0L

    suspend operator fun invoke(story: Story): DomainResult<Unit> {
        if (story.text.length < MIN_TEXT_LENGTH) {
            return DomainResult.Failure(
                DomainError(DomainErrorType.INVALID_INPUT, "Рассказ слишком короткий")
            )
        }
        val now = clock()
        if (now - lastSpokenAt < MIN_INTERVAL_MS) {
            return DomainResult.Failure(
                DomainError(DomainErrorType.INVALID_INPUT, "Слишком часто")
            )
        }
        try {
            ttsRepository.speak(story.text, story.language)
        } catch (e: Exception) {
            return DomainResult.Failure(
                DomainError(DomainErrorType.TTS, "Ошибка озвучки", e)
            )
        }
        lastSpokenAt = now
        return DomainResult.Success(Unit)
    }

    companion object {
        const val MIN_TEXT_LENGTH = 50
        const val MIN_INTERVAL_MS = 2_000L
    }
}