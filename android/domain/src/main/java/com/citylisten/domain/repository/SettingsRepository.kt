package com.citylisten.domain.repository

import com.citylisten.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeLocationConsent(): Flow<Boolean>
    suspend fun setLocationConsent(consent: Boolean)

    fun observeLanguage(): Flow<String>
    suspend fun setLanguage(language: String)

    fun observeOnboardingCompleted(): Flow<Boolean>
    suspend fun setOnboardingCompleted(completed: Boolean)

    fun observeThemeMode(): Flow<ThemeMode>
    suspend fun setThemeMode(mode: ThemeMode)

    fun observeProfileName(): Flow<String>
    suspend fun setProfileName(name: String)
}
