package com.citylisten.presentation.profile

data class ProfileUiState(
    val isLoading: Boolean = true,
    val displayName: String = "",
    val savedName: String = "",
    val error: String? = null,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false
)
