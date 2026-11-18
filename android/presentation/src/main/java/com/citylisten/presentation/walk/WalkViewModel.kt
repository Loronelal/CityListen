package com.citylisten.presentation.walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citylisten.domain.usecase.GetLastStoryUseCase
import com.citylisten.domain.usecase.ObserveLocationConsentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WalkViewModel @Inject constructor(
    private val getLastStory: GetLastStoryUseCase,
    private val observeLocationConsent: ObserveLocationConsentUseCase
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)

    val uiState: StateFlow<WalkUiState> = retryTrigger
        .flatMapLatest {
            combine(
                getLastStory(),
                observeLocationConsent()
            ) { story, consent ->
                if (story == null) {
                    WalkUiState.Empty(locationConsent = consent)
                } else {
                    WalkUiState.Content(story = story, locationConsent = consent)
                }
            }
                .onStart { emit(WalkUiState.Loading) }
                .catch { throwable -> emit(WalkUiState.Error(throwable.toWalkError())) }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = WalkUiState.Loading
        )

    fun retry() {
        retryTrigger.update { attempt -> attempt + 1 }
    }

    private fun Throwable.toWalkError(): WalkError = when (this) {
        is SocketTimeoutException -> WalkError.TIMEOUT
        is IOException -> WalkError.NETWORK
        else -> WalkError.UNKNOWN
    }

    companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
