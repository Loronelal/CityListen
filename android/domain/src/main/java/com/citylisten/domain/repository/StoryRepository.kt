package com.citylisten.domain.repository

import com.citylisten.domain.model.Place
import com.citylisten.domain.model.Story
import kotlinx.coroutines.flow.Flow

interface StoryRepository {
    suspend fun generate(place: Place, language: String): Story
    suspend fun save(story: Story)
    suspend fun getCached(placeId: String, language: String): Story?
    fun observeHistory(): Flow<List<Story>>
}