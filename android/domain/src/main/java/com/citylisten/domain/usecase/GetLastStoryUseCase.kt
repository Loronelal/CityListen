package com.citylisten.domain.usecase

import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetLastStoryUseCase @Inject constructor(
    private val storyRepository: StoryRepository
) {
    operator fun invoke(): Flow<Story?> =
        storyRepository.observeHistory()
            .map { stories -> stories.maxByOrNull { it.createdAt } }
}
