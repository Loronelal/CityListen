package com.citylisten.presentation.walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citylisten.domain.result.LocationUnavailableException
import com.citylisten.domain.usecase.GetLastStoryUseCase
import com.citylisten.domain.usecase.ObserveLocationConsentUseCase
import com.citylisten.domain.usecase.StartWalkUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.SocketTimeoutException
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class WalkViewModel @Inject constructor(
    private val getLastStory: GetLastStoryUseCase,
    private val observeLocationConsent: ObserveLocationConsentUseCase,
    private val startWalkUseCase: StartWalkUseCase
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)
    private val _locationState = MutableStateFlow(WalkLocationUiState())
    private var locationJob: Job? = null

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

    val locationState: StateFlow<WalkLocationUiState> = _locationState.asStateFlow()

    fun startWalk() {
        if (_locationState.value.isWalking) return

        _locationState.value = WalkLocationUiState(isWalking = true)
        subscribeToLocation()
    }

    fun pauseWalk() {
        if (!_locationState.value.isWalking) return

        locationJob?.cancel()
        locationJob = null
    }

    fun resumeWalk() {
        if (!_locationState.value.isWalking || locationJob != null) return

        _locationState.update { it.copy(error = null) }
        subscribeToLocation()
    }

    fun stopWalk() {
        locationJob?.cancel()
        locationJob = null
        _locationState.value = WalkLocationUiState()
    }

    private fun subscribeToLocation() {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            startWalkUseCase()
                .catch { throwable ->
                    _locationState.update {
                        it.copy(
                            isWalking = false,
                            error = throwable.toLocationError()
                        )
                    }
                }
                .onCompletion { cause ->
                    if (cause == null) {
                        _locationState.update { it.copy(isWalking = false) }
                    }
                }
                .collect { point ->
                    _locationState.update {
                        it.copy(point = point, error = null)
                    }
                }
        }
    }

    fun retry() {
        retryTrigger.update { attempt -> attempt + 1 }
    }

    private fun Throwable.toWalkError(): WalkError = when (this) {
        is SocketTimeoutException -> WalkError.TIMEOUT
        is IOException -> WalkError.NETWORK
        else -> WalkError.UNKNOWN
    }

    private fun Throwable.toLocationError(): WalkLocationError = when (this) {
        is SecurityException -> WalkLocationError.PERMISSION
        is LocationUnavailableException -> WalkLocationError.UNAVAILABLE
        else -> WalkLocationError.UNAVAILABLE
    }

    companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
