package com.citylisten.presentation.history

import com.citylisten.domain.model.Story
import com.citylisten.presentation.ui.UiError
import java.time.LocalDate

sealed interface HistoryUiState {

    data object Loading : HistoryUiState

    data class Content(val days: List<HistoryDay>) : HistoryUiState

    data object Empty : HistoryUiState

    data class Error(
        val error: UiError,
        val cachedDays: List<HistoryDay> = emptyList()
    ) : HistoryUiState
}

data class HistoryDay(
    val date: LocalDate,
    val stories: List<Story>
)
