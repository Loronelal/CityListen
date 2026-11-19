package com.citylisten.presentation.walk

import com.citylisten.domain.model.GeoPoint

data class WalkLocationUiState(
    val isWalking: Boolean = false,
    val point: GeoPoint? = null,
    val error: WalkLocationError? = null
)

enum class WalkLocationError {
    PERMISSION,
    UNAVAILABLE
}
