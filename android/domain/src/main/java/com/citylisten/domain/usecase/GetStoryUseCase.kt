package com.citylisten.domain.usecase

import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.StoryRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetStoryUseCase @Inject constructor(
    private val storyRepository: StoryRepository
) {
    suspend operator fun invoke(storyId: String): Story? =
        storyRepository.observeHistory()
            .first()
            .firstOrNull { it.id == storyId }
}
