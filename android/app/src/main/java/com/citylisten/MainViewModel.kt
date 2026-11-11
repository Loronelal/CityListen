package com.citylisten

import androidx.lifecycle.ViewModel
import com.citylisten.domain.CityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val repository: CityRepository
) : ViewModel() {
    val welcomeText = repository.getWelcomeText()
}