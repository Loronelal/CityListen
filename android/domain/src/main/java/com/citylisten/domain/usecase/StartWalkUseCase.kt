package com.citylisten.domain.usecase

import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.repository.LocationRepository
import com.citylisten.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class StartWalkUseCase(
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<GeoPoint> =
        settingsRepository.observeLocationConsent()
            .flatMapLatest { consent ->
                if (consent) locationRepository.observeLocation() else flowOf()
            }
}