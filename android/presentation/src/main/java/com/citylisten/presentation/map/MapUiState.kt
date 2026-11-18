package com.citylisten.presentation.map

import com.citylisten.domain.model.Story
import com.citylisten.presentation.ui.UiError

sealed interface MapUiState {

    data object Loading : MapUiState

    data class Content(val places: List<Story>) : MapUiState

    data object Empty : MapUiState

    data class Error(val error: UiError) : MapUiState
}
