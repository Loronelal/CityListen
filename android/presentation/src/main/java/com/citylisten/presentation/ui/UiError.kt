package com.citylisten.presentation.ui

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
    is SocketTimeoutException -> UiError.TIMEOUT
    is IOException -> UiError.NETWORK
    else -> UiError.UNKNOWN
}

fun UiError.messageRes(): Int = when (this) {
    UiError.NETWORK -> R.string.error_network
    UiError.TIMEOUT -> R.string.error_timeout
    UiError.SERVER -> R.string.error_server
    UiError.UNKNOWN -> R.string.error_unknown
}
