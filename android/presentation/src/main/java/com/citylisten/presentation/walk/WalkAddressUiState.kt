package com.citylisten.presentation.walk

import com.citylisten.domain.model.Address
import com.citylisten.presentation.ui.UiError

sealed interface WalkAddressUiState {

    data object Idle : WalkAddressUiState

    data object Loading : WalkAddressUiState

    data class Content(val address: Address) : WalkAddressUiState

    data class Error(val error: UiError) : WalkAddressUiState
}
