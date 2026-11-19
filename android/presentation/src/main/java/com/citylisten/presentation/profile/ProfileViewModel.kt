package com.citylisten.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citylisten.domain.result.DomainResult
import com.citylisten.domain.usecase.ObserveProfileUseCase
import com.citylisten.domain.usecase.SaveProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeProfile: ObserveProfileUseCase,
    private val saveProfile: SaveProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val savedName = observeProfile()
                .catch { emit("") }
                .first()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    displayName = savedName,
                    savedName = savedName
                )
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(
                displayName = name,
                error = null,
                isSaved = false
            )
        }
    }

    fun save() {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSaving = true,
                    error = null,
                    isSaved = false
                )
            }
            when (val result = saveProfile(_uiState.value.displayName)) {
                is DomainResult.Success -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            displayName = result.data,
                            savedName = result.data,
                            isSaved = true
                        )
                    }
                }
                is DomainResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            isSaving = false,
                            error = result.error.message
                        )
                    }
                }
            }
        }
    }
}
