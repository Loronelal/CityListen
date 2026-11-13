package com.citylisten.domain.usecase

import com.citylisten.domain.model.Place
import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.StoryRepository
import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult

class GenerateStoryUseCase(
    private val storyRepository: StoryRepository,
    private val enrichPlaceUseCase: EnrichPlaceUseCase
) {
    suspend operator fun invoke(place: Place, language: String): DomainResult<Story> {
        storyRepository.getCached(place.id, language)?.let {
            return DomainResult.Success(it)
        }
        val enriched = enrichPlaceUseCase(place)
        val story = try {
            storyRepository.generate(enriched, language)
        } catch (e: Exception) {
            return DomainResult.Failure(
                DomainError(DomainErrorType.SERVER, "Не удалось сгенерировать рассказ", e)
            )
        }
        storyRepository.save(story)
        return DomainResult.Success(story)
    }
}