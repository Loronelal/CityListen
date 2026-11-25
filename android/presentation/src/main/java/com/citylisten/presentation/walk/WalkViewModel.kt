package com.citylisten.presentation.walk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.result.DomainResult
import com.citylisten.domain.result.LocationUnavailableException
import com.citylisten.domain.usecase.GetAddressUseCase
import com.citylisten.domain.usecase.GetLastStoryUseCase
import com.citylisten.domain.usecase.ObserveLanguageUseCase
import com.citylisten.domain.usecase.ObserveLocationConsentUseCase
import com.citylisten.domain.usecase.StartWalkUseCase
import com.citylisten.presentation.ui.toUiError
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
    private val startWalkUseCase: StartWalkUseCase,
    private val getAddress: GetAddressUseCase,
    observeLanguage: ObserveLanguageUseCase
) : ViewModel() {

    private val retryTrigger = MutableStateFlow(0)
    private val _locationState = MutableStateFlow(WalkLocationUiState())
    private val _addressState = MutableStateFlow<WalkAddressUiState>(WalkAddressUiState.Idle)
    private var locationJob: Job? = null
    private var addressJob: Job? = null
    private var lastRequestedPoint: GeoPoint? = null
    private var pendingPoint: GeoPoint? = null
    private var language: String = DEFAULT_LANGUAGE

    init {
        viewModelScope.launch {
            observeLanguage().collect { language = it }
        }
    }

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

    val addressState: StateFlow<WalkAddressUiState> = _addressState.asStateFlow()

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
        addressJob?.cancel()
        addressJob = null
        lastRequestedPoint = null
        pendingPoint = null
        _addressState.value = WalkAddressUiState.Idle
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
                    loadAddress(point)
                }
        }
    }

    private fun loadAddress(point: GeoPoint) {
        val last = lastRequestedPoint
        if (last != null && last.distanceTo(point) < ADDRESS_REFRESH_METERS) return
        requestAddress(point)
    }

    private fun requestAddress(point: GeoPoint) {
        pendingPoint = point
        lastRequestedPoint = point
        addressJob?.cancel()
        addressJob = viewModelScope.launch {
            _addressState.value = WalkAddressUiState.Loading
            when (val result = getAddress(point, language)) {
                is DomainResult.Success ->
                    _addressState.value = WalkAddressUiState.Content(result.data)
                is DomainResult.Failure ->
                    _addressState.value = WalkAddressUiState.Error(result.error.toUiError())
            }
        }
    }

    fun retryAddress() {
        val point = pendingPoint ?: return
        requestAddress(point)
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
        const val ADDRESS_REFRESH_METERS = 100.0
        private const val DEFAULT_LANGUAGE = "ru"
    }
}
