package com.citylisten.presentation.ui

import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainException
import com.citylisten.presentation.R
import java.io.IOException
import java.net.SocketTimeoutException

enum class UiError {
    NETWORK,
    TIMEOUT,
    SERVER,
    UNKNOWN
}

fun Throwable.toUiError(): UiError = when (this) {
    is DomainException -> error.toUiError()
    is SocketTimeoutException -> UiError.TIMEOUT
    is IOException -> UiError.NETWORK
    else -> UiError.UNKNOWN
}

fun DomainError.toUiError(): UiError = when (type) {
    DomainErrorType.NETWORK -> UiError.NETWORK
    DomainErrorType.TIMEOUT -> UiError.TIMEOUT
    DomainErrorType.SERVER -> UiError.SERVER
    else -> UiError.UNKNOWN
}

fun UiError.messageRes(): Int = when (this) {
    UiError.NETWORK -> R.string.error_network
    UiError.TIMEOUT -> R.string.error_timeout
    UiError.SERVER -> R.string.error_server
    UiError.UNKNOWN -> R.string.error_unknown
}
