package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakePlaceRepository
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.PlaceType
import com.citylisten.domain.result.DomainResult
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DetectNearbyPlaceUseCaseTest {

    private val point = GeoPoint(55.75, 37.61)

    @Test
    fun `filters unknown types and limits to 5 places`() = runTest {
        val places = (1..10).map {
            Place(
                id = "p$it",
                name = "Place $it",
                type = PlaceType.BUILDING,
                location = GeoPoint(55.75, 37.61)
            )
        } + Place("x", "Unknown", PlaceType.UNKNOWN, point)
        val repo = FakePlaceRepository(nearbyPlaces = places)
        val useCase = DetectNearbyPlaceUseCase(repo)

        val result = useCase(point)

        assertTrue(result is DomainResult.Success)
        val data = (result as DomainResult.Success).data
        assertEquals(5, data.size)
        assertTrue(data.none { it.type == PlaceType.UNKNOWN })
    }

    @Test
    fun `returns Failure on network error`() = runTest {
        val repo = FakePlaceRepository(throwOnFind = true)
        val useCase = DetectNearbyPlaceUseCase(repo)

        val result = useCase(point)

        assertTrue(result is DomainResult.Failure)
    }
}