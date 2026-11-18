package com.citylisten.domain.usecase

import com.citylisten.domain.fake.FakeSettingsRepository
import com.citylisten.domain.model.ThemeMode
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsUseCasesTest {

    @Test
    fun `completing onboarding stores the flag`() = runTest {
        val repository = FakeSettingsRepository(onboardingCompleted = false)

        CompleteOnboardingUseCase(repository)()

        assertTrue(repository.onboardingCompleted)
        assertTrue(ObserveOnboardingCompletedUseCase(repository)().first())
    }

    @Test
    fun `theme mode is stored and observed`() = runTest {
        val repository = FakeSettingsRepository(themeMode = ThemeMode.SYSTEM)

        SetThemeModeUseCase(repository)(ThemeMode.DARK)

        assertEquals(ThemeMode.DARK, repository.themeMode)
        assertEquals(ThemeMode.DARK, ObserveThemeModeUseCase(repository)().first())
    }

    @Test
    fun `location consent is stored`() = runTest {
        val repository = FakeSettingsRepository(consent = true)

        SetLocationConsentUseCase(repository)(false)

        assertFalse(repository.consent)
        assertFalse(ObserveLocationConsentUseCase(repository)().first())
    }

    @Test
    fun `language is observed`() = runTest {
        val repository = FakeSettingsRepository(language = "en")

        assertEquals("en", ObserveLanguageUseCase(repository)().first())
    }

    @Test
    fun `profile name is observed`() = runTest {
        val repository = FakeSettingsRepository(profileName = "Анна")

        assertEquals("Анна", ObserveProfileUseCase(repository)().first())
    }
}
