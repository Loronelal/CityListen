package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakePlaceRepository
import com.citylisten.domain.fake.FakeStoryRepository
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.PlaceType
import com.citylisten.domain.model.Story
import com.citylisten.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GenerateStoryUseCaseTest {

    private val place = Place("p1", "Test", PlaceType.BUILDING, GeoPoint(55.75, 37.61))

    @Test
    fun `returns cached story when available`() = runTest {
        val cached = Story("s1", "p1", "Title", "Text", "ru", Instant.now(), cached = true)
        val storyRepo = FakeStoryRepository(cached = cached)
        val useCase = GenerateStoryUseCase(storyRepo, EnrichPlaceUseCase(FakePlaceRepository()))

        val result = useCase(place, "ru")

        assertTrue(result is DomainResult.Success)
        assertEquals("s1", (result as DomainResult.Success).data.id)
        assertTrue(storyRepo.savedStories.isEmpty())
    }

    @Test
    fun `generates and saves new story when cache is empty`() = runTest {
        val storyRepo = FakeStoryRepository(cached = null)
        val useCase = GenerateStoryUseCase(storyRepo, EnrichPlaceUseCase(FakePlaceRepository()))

        val result = useCase(place, "ru")

        assertTrue(result is DomainResult.Success)
        assertEquals(1, storyRepo.savedStories.size)
    }

    @Test
    fun `returns Failure when generation throws`() = runTest {
        val storyRepo = FakeStoryRepository(generateThrows = true)
        val useCase = GenerateStoryUseCase(storyRepo, EnrichPlaceUseCase(FakePlaceRepository()))

        val result = useCase(place, "ru")

        assertTrue(result is DomainResult.Failure)
    }
}