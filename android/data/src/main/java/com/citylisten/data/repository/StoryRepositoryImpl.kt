package com.citylisten.data.repository

import com.citylisten.data.local.StoryDao
import com.citylisten.data.local.toDomain
import com.citylisten.data.local.toEntity
import com.citylisten.data.remote.api.CityListenApi
import com.citylisten.data.remote.dto.GenerateStoryRequest
import com.citylisten.data.remote.mapper.toDomain
import com.citylisten.data.remote.safeApiCall
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.StoryRepository
import com.citylisten.domain.result.DomainResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class StoryRepositoryImpl @Inject constructor(
    private val api: CityListenApi,
    private val storyDao: StoryDao
) : StoryRepository {

    override suspend fun generate(place: Place, language: String): Story {
        val request = GenerateStoryRequest(place.id, language)
        val result = safeApiCall { api.generateStory(request) }

        return when (result) {
            is DomainResult.Success -> {
                val story = result.data.toDomain()
                storyDao.insert(story.toEntity())
                story
            }
            is DomainResult.Failure -> throw RuntimeException(result.error.message)
        }
    }

    override suspend fun save(story: Story) {
        storyDao.insert(story.toEntity())
    }

    override suspend fun getCached(placeId: String, language: String): Story? {
        return storyDao.getCached(placeId, language)?.toDomain()
    }

    override fun observeHistory(): Flow<List<Story>> {
        return storyDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}