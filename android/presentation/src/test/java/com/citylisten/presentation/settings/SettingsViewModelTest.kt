package com.citylisten.presentation.settings

import app.cash.turbine.test
import com.citylisten.domain.model.ThemeMode
import com.citylisten.domain.usecase.ChangeLanguageUseCase
import com.citylisten.domain.usecase.ObserveLanguageUseCase
import com.citylisten.domain.usecase.ObserveLocationConsentUseCase
import com.citylisten.domain.usecase.ObserveProfileUseCase
import com.citylisten.domain.usecase.ObserveThemeModeUseCase
import com.citylisten.domain.usecase.SetThemeModeUseCase
import com.citylisten.presentation.MainDispatcherRule
import com.citylisten.presentation.testing.FakeSettingsRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `exposes saved language theme consent and profile`() = runTest {
        val repository = FakeSettingsRepository(
            consent = true,
            language = "en",
            themeMode = ThemeMode.DARK,
            profileName = "Анна"
        )
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }

            assertEquals("en", state.language)
            assertEquals(ThemeMode.DARK, state.themeMode)
            assertTrue(state.locationConsent)
            assertEquals("Анна", state.profileName)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `changing language stores it`() = runTest {
        val repository = FakeSettingsRepository(language = "ru")
        val viewModel = createViewModel(repository)

        viewModel.setLanguage("en")

        assertEquals("en", repository.language)
    }

    @Test
    fun `changing theme stores it`() = runTest {
        val repository = FakeSettingsRepository(themeMode = ThemeMode.SYSTEM)
        val viewModel = createViewModel(repository)

        viewModel.setThemeMode(ThemeMode.LIGHT)

        assertEquals(ThemeMode.LIGHT, repository.themeMode)
    }

    @Test
    fun `location consent is reflected in settings`() = runTest {
        val repository = FakeSettingsRepository(consent = false)
        val viewModel = createViewModel(repository)

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) {
                state = awaitItem()
            }
            assertFalse(state.locationConsent)
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createViewModel(repository: FakeSettingsRepository): SettingsViewModel {
        return SettingsViewModel(
            observeLanguage = ObserveLanguageUseCase(repository),
            observeThemeMode = ObserveThemeModeUseCase(repository),
            observeLocationConsent = ObserveLocationConsentUseCase(repository),
            observeProfile = ObserveProfileUseCase(repository),
            changeLanguage = ChangeLanguageUseCase(repository),
            updateThemeMode = SetThemeModeUseCase(repository)
        )
    }
}
