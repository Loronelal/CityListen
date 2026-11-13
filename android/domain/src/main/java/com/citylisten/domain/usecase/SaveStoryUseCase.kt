package com.citylisten.domain.usecase

import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.StoryRepository
import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult

class SaveStoryUseCase(
    private val storyRepository: StoryRepository
) {
    suspend operator fun invoke(story: Story): DomainResult<Unit> {
        if (story.text.isBlank() || story.title.isBlank() || story.placeId.isBlank()) {
            return DomainResult.Failure(
                DomainError(DomainErrorType.INVALID_INPUT, "Пустой рассказ")
            )
        }
        val normalized = if (story.text.length > MAX_TEXT_LENGTH) {
            story.copy(text = story.text.take(MAX_TEXT_LENGTH))
        } else story
        storyRepository.save(normalized)
        return DomainResult.Success(Unit)
    }

    companion object {
        const val MAX_TEXT_LENGTH = 5_000
    }
}