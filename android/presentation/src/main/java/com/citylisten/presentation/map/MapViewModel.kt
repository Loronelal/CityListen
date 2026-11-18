package com.citylisten.presentation.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MapViewModel @Inject constructor(
    private val getHistory: GetHistoryUseCase
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<MapUiState> = retryTrigger
        .flatMapLatest {
            getHistory()
                .map { stories ->
                    val places = stories.distinctBy { it.placeId }
                    if (places.isEmpty()) MapUiState.Empty else MapUiState.Content(places)
                }
                .onStart { emit(MapUiState.Loading) }
                .catch { throwable -> emit(MapUiState.Error(throwable.toUiError())) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = MapUiState.Loading
        )

    fun retry() {
        retryTrigger.update { attempt -> attempt + 1 }
    }

    companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
