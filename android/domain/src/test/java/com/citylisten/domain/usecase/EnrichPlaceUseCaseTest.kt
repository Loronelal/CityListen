package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakePlaceRepository
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.PlaceType
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class EnrichPlaceUseCaseTest {

    private val place = Place("p1", "Test", PlaceType.BUILDING, GeoPoint(55.75, 37.61))

    @Test
    fun `returns enriched place when repository succeeds`() = runTest {
        val repo = FakePlaceRepository()
        val useCase = EnrichPlaceUseCase(repo)

        val result = useCase(place)

        assertEquals(place.id, result.id)
    }

    @Test
    fun `returns original place when repository fails`() = runTest {
        val repo = FakePlaceRepository(throwOnEnrich = true)
        val useCase = EnrichPlaceUseCase(repo)

        val result = useCase(place)

        assertEquals(place, result)
    }
}