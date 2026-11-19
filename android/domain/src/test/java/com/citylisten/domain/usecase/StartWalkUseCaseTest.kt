package com.citylisten.domain.usecase

import app.cash.turbine.test
import com.citylisten.domain.fake.FakeLocationRepository
import com.citylisten.domain.fake.FakeSettingsRepository
import com.citylisten.domain.model.GeoPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StartWalkUseCaseTest {

    @Test
    fun `emits location when consent is granted`() = runTest {
        val locationRepo = FakeLocationRepository(listOf(GeoPoint(55.75, 37.61)))
        val settingsRepo = FakeSettingsRepository(consent = true)
        val useCase = StartWalkUseCase(locationRepo, settingsRepo)

        val result = useCase().take(1).toList()
        assertEquals(1, result.size)
        assertEquals(55.75, result[0].latitude, 0.001)
    }

    @Test
    fun `emits nothing when consent is denied`() = runTest {
        val locationRepo = FakeLocationRepository(listOf(GeoPoint(55.75, 37.61)))
        val settingsRepo = FakeSettingsRepository(consent = false)
        val useCase = StartWalkUseCase(locationRepo, settingsRepo)

        useCase().test {
            runCurrent()
            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `emits new points while walking`() = runTest {
        val locationRepo = FakeLocationRepository(emptyList())
        val settingsRepo = FakeSettingsRepository(consent = true)
        val useCase = StartWalkUseCase(locationRepo, settingsRepo)
        val point = GeoPoint(55.76, 37.62)

        useCase().test {
            runCurrent()
            locationRepo.emit(point)

            assertEquals(point, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `stops emitting when consent is revoked`() = runTest {
        val locationRepo = FakeLocationRepository(emptyList())
        val settingsRepo = FakeSettingsRepository(consent = true)
        val useCase = StartWalkUseCase(locationRepo, settingsRepo)
        val first = GeoPoint(55.76, 37.62)
        val second = GeoPoint(55.77, 37.63)

        useCase().test {
            runCurrent()
            locationRepo.emit(first)
            assertEquals(first, awaitItem())

            settingsRepo.setLocationConsent(false)
            runCurrent()
            locationRepo.emit(second)

            expectNoEvents()
            cancelAndIgnoreRemainingEvents()
        }
    }
}