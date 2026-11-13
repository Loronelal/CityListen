package com.citylisten.domain.repository

import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observeLocationConsent(): Flow<Boolean>
    suspend fun setLocationConsent(consent: Boolean)

    fun observeLanguage(): Flow<String>
    suspend fun setLanguage(language: String)
}