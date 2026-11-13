package com.citylisten.domain.result

sealed interface DomainResult<out T> {
    data class Success<T>(val data: T) : DomainResult<T>
    data class Failure(val error: DomainError) : DomainResult<Nothing>
}

enum class DomainErrorType {
    NETWORK,
    TIMEOUT,
    SERVER,
    CACHE_MISS,
    PERMISSION,
    TTS,
    INVALID_INPUT,
    UNKNOWN
}

data class DomainError(
    val type: DomainErrorType,
    val message: String,
    val cause: Throwable? = null
)