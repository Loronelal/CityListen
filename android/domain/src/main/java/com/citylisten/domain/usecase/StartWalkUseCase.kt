package com.citylisten.domain.usecase

import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.repository.LocationRepository
import com.citylisten.domain.repository.SettingsRepository
import com.citylisten.domain.result.LocationUnavailableException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class StartWalkUseCase @Inject constructor(
    private val locationRepository: LocationRepository,
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<GeoPoint> =
        settingsRepository.observeLocationConsent()
            .flatMapLatest { consent ->
                when {
                    !consent -> flowOf()
                    !locationRepository.isLocationAvailable() -> flow {
                        throw LocationUnavailableException()
                    }
                    else -> locationRepository.observeLocation()
                }
            }
}