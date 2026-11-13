package com.citylisten.domain.fake

import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.Story
import com.citylisten.domain.repository.LocationRepository
import com.citylisten.domain.repository.PlaceRepository
import com.citylisten.domain.repository.SettingsRepository
import com.citylisten.domain.repository.StoryRepository
import com.citylisten.domain.repository.TtsRepository
import com.citylisten.domain.repository.TtsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.Instant

class FakeLocationRepository(
    private val points: List<GeoPoint> = listOf(GeoPoint(55.75, 37.61))
) : LocationRepository {
    override fun observeLocation(): Flow<GeoPoint> = flowOf(*points.toTypedArray())
}

class FakeSettingsRepository(
    var consent: Boolean = true,
    var language: String = "ru"
) : SettingsRepository {
    private val consentFlow = MutableStateFlow(consent)
    private val languageFlow = MutableStateFlow(language)

    override fun observeLocationConsent(): Flow<Boolean> = consentFlow
    override suspend fun setLocationConsent(consent: Boolean) { this.consent = consent; consentFlow.value = consent }
    override fun observeLanguage(): Flow<String> = languageFlow
    override suspend fun setLanguage(language: String) { this.language = language; languageFlow.value = language }
}

class FakePlaceRepository(
    var nearbyPlaces: List<Place> = emptyList(),
    var throwOnFind: Boolean = false,
    var throwOnEnrich: Boolean = false
) : PlaceRepository {
    override suspend fun findNearby(point: GeoPoint, radiusMeters: Int): List<Place> {
        if (throwOnFind) throw RuntimeException("Network error")
        return nearbyPlaces
    }
    override suspend fun enrich(place: Place): Place {
        if (throwOnEnrich) throw RuntimeException("Enrich error")
        return place.copy(facts = listOf())
    }
}

open class FakeStoryRepository(
    var cached: Story? = null,
    var generateThrows: Boolean = false,
    private var history: MutableStateFlow<List<Story>> = MutableStateFlow(emptyList())
) : StoryRepository {
    val savedStories = mutableListOf<Story>()

    override suspend fun generate(place: Place, language: String): Story {
        if (generateThrows) throw RuntimeException("Server error")
        return Story(
            id = "story_${place.id}",
            placeId = place.id,
            title = "Рассказ",
            text = "Это достаточно длинный текст рассказа для проверки.",
            language = language,
            createdAt = Instant.parse("2024-01-01T12:00:00Z")
        )
    }
    override suspend fun save(story: Story) { savedStories.add(story) }
    override suspend fun getCached(placeId: String, language: String): Story? = cached
    override fun observeHistory(): Flow<List<Story>> = history
}

class FakeTtsRepository : TtsRepository {
    var spokenText: String? = null
    override suspend fun speak(text: String, language: String) { spokenText = text }
    override fun pause() {}
    override fun resume() {}
    override fun stop() {}
    override fun observeState(): Flow<TtsState> = flowOf(TtsState.IDLE)
}