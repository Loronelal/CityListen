package com.citylisten.presentation.walk

import com.citylisten.domain.model.Story

sealed interface WalkUiState {

    data object Loading : WalkUiState

    data class Empty(val locationConsent: Boolean) : WalkUiState

    data class Content(
        val story: Story,
        val locationConsent: Boolean
    ) : WalkUiState

    data class Error(val error: WalkError) : WalkUiState
}

enum class WalkError {
    NETWORK,
    TIMEOUT,
    SERVER,
    UNKNOWN
}
