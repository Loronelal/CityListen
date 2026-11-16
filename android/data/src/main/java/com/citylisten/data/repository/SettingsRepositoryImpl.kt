package com.citylisten.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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
}