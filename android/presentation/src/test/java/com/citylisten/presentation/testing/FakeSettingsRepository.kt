package com.citylisten.presentation.testing

import com.citylisten.domain.model.ThemeMode
import com.citylisten.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSettingsRepository(
    consent: Boolean = false,
    language: String = "ru",
    onboardingCompleted: Boolean = false,
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    profileName: String = ""
) : SettingsRepository {

    var consent: Boolean = consent
        private set
    var language: String = language
        private set
    var onboardingCompleted: Boolean = onboardingCompleted
        private set
    var themeMode: ThemeMode = themeMode
        private set
    var profileName: String = profileName
        private set

    private val consentFlow = MutableStateFlow(consent)
    private val languageFlow = MutableStateFlow(language)
    private val onboardingFlow = MutableStateFlow(onboardingCompleted)
    private val themeFlow = MutableStateFlow(themeMode)
    private val profileFlow = MutableStateFlow(profileName)

    override fun observeLocationConsent(): Flow<Boolean> = consentFlow

    override suspend fun setLocationConsent(consent: Boolean) {
        this.consent = consent
        consentFlow.value = consent
    }

    override fun observeLanguage(): Flow<String> = languageFlow

    override suspend fun setLanguage(language: String) {
        this.language = language
        languageFlow.value = language
    }

    override fun observeOnboardingCompleted(): Flow<Boolean> = onboardingFlow

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        onboardingCompleted = completed
        onboardingFlow.value = completed
    }

    override fun observeThemeMode(): Flow<ThemeMode> = themeFlow

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeMode = mode
        themeFlow.value = mode
    }

    override fun observeProfileName(): Flow<String> = profileFlow

    override suspend fun setProfileName(name: String) {
        profileName = name
        profileFlow.value = name
    }
}
