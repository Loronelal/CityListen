package com.citylisten.domain.repository

import kotlinx.coroutines.flow.Flow

interface TtsRepository {
    suspend fun speak(text: String, language: String)
    fun pause()
    fun resume()
    fun stop()
    fun observeState(): Flow<TtsState>
}

enum class TtsState { IDLE, SPEAKING, PAUSED, ERROR }