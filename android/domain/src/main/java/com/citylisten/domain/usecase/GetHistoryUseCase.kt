package com.citylisten.domain.usecase

import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.StoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetHistoryUseCase(
    private val storyRepository: StoryRepository
) {
    operator fun invoke(): Flow<List<Story>> =
        storyRepository.observeHistory()
            .map { all ->
                all.sortedByDescending { it.createdAt }
                    .distinctBy { it.placeId to it.language }
                    .take(MAX_ITEMS)
            }

    companion object {
        const val MAX_ITEMS = 50
    }
}