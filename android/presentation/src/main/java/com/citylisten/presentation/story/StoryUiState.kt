package com.citylisten.presentation.story

import com.citylisten.domain.model.Story

sealed interface StoryUiState {

    data object Loading : StoryUiState

    data class Content(val story: Story) : StoryUiState

    data object NotFound : StoryUiState
}
