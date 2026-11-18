package com.citylisten.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.citylisten.domain.model.ThemeMode
import com.citylisten.domain.repository.SettingsRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val CONSENT = booleanPreferencesKey("location_consent")
        val LANGUAGE = stringPreferencesKey("language")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PROFILE_NAME = stringPreferencesKey("profile_name")
    }

    override fun observeLocationConsent(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.CONSENT] ?: false
        }
    }

    override suspend fun setLocationConsent(consent: Boolean) {
        context.dataStore.edit { it[Keys.CONSENT] = consent }
    }

    override fun observeLanguage(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.LANGUAGE] ?: "ru"
        }
    }

    override suspend fun setLanguage(language: String) {
        context.dataStore.edit { it[Keys.LANGUAGE] = language }
    }

    override fun observeOnboardingCompleted(): Flow<Boolean> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] ?: false
        }
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[Keys.ONBOARDING_COMPLETED] = completed }
    }

    override fun observeThemeMode(): Flow<ThemeMode> {
        return context.dataStore.data.map { prefs ->
            ThemeMode.entries.firstOrNull { it.name == prefs[Keys.THEME_MODE] }
                ?: ThemeMode.SYSTEM
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { it[Keys.THEME_MODE] = mode.name }
    }

    override fun observeProfileName(): Flow<String> {
        return context.dataStore.data.map { prefs ->
            prefs[Keys.PROFILE_NAME] ?: ""
        }
    }

    override suspend fun setProfileName(name: String) {
        context.dataStore.edit { it[Keys.PROFILE_NAME] = name }
    }
}
