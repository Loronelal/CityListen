package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakeSettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ObserveLocationConsentUseCaseTest {

    @Test
    fun `emits stored consent value`() = runTest {
        val repository = FakeSettingsRepository(consent = true)

        val result = ObserveLocationConsentUseCase(repository)().first()

        assertTrue(result)
    }

    @Test
    fun `emits false when consent is not granted`() = runTest {
        val repository = FakeSettingsRepository(consent = false)

        val result = ObserveLocationConsentUseCase(repository)().first()

        assertFalse(result)
    }
}
