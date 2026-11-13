package com.citylisten.domain.usecase

import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.model.Place
import com.citylisten.domain.model.PlaceType
import com.citylisten.domain.repository.PlaceRepository
import com.citylisten.domain.result.DomainError
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult

class DetectNearbyPlaceUseCase(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(point: GeoPoint): DomainResult<List<Place>> {
        val places = try {
            placeRepository.findNearby(point, RADIUS_METERS)
        } catch (e: Exception) {
            return DomainResult.Failure(
                DomainError(DomainErrorType.NETWORK, "Не удалось найти объекты рядом", e)
            )
        }
        val filtered = places
            .filter { it.type != PlaceType.UNKNOWN }
            .sortedBy { point.distanceTo(it.location) }
            .take(MAX_PLACES)
        return DomainResult.Success(filtered)
    }

    companion object {
        const val RADIUS_METERS = 200
        const val MAX_PLACES = 5
    }
}