package com.citylisten.presentation.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citylisten.domain.usecase.SetLocationConsentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val setLocationConsent: SetLocationConsentUseCase
) : ViewModel() {

    fun setConsent(granted: Boolean) {
        viewModelScope.launch {
            setLocationConsent(granted)
        }
    }
}
