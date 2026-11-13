package com.citylisten.domain.usecase

import com.citylisten.domain.model.Place
import com.citylisten.domain.repository.PlaceRepository

class EnrichPlaceUseCase(
    private val placeRepository: PlaceRepository
) {
    suspend operator fun invoke(place: Place): Place =
        runCatching { placeRepository.enrich(place) }
            .getOrElse { place }
}