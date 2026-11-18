package com.citylisten.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citylisten.domain.model.Story
import com.citylisten.domain.usecase.GetHistoryUseCase
import com.citylisten.presentation.ui.toUiError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.ZoneId
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistory: GetHistoryUseCase
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)
    private var cachedDays: List<HistoryDay> = emptyList()

    val uiState: StateFlow<HistoryUiState> = retryTrigger
        .flatMapLatest {
            getHistory()
                .map { stories ->
                    val days = stories.toDays()
                    cachedDays = days
                    if (days.isEmpty()) HistoryUiState.Empty else HistoryUiState.Content(days)
                }
                .onStart { emit(HistoryUiState.Loading) }
                .catch { throwable ->
                    emit(HistoryUiState.Error(throwable.toUiError(), cachedDays))
                }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HistoryUiState.Loading
        )

    fun retry() {
        retryTrigger.update { attempt -> attempt + 1 }
    }

    companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

private fun List<Story>.toDays(): List<HistoryDay> {
    val zone = ZoneId.systemDefault()
    return groupBy { it.createdAt.atZone(zone).toLocalDate() }
        .toSortedMap(compareByDescending { it })
        .map { (date, stories) ->
            HistoryDay(
                date = date,
                stories = stories.sortedByDescending { it.createdAt }
            )
        }
}
