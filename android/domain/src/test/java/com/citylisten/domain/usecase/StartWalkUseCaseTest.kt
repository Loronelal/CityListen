package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakeLocationRepository
import com.citylisten.domain.fake.FakeSettingsRepository
import com.citylisten.domain.model.GeoPoint
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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

        val result = useCase().take(1).toList()

        assertTrue(result.isEmpty())
    }
}