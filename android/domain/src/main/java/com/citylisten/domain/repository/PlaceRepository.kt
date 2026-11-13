package com.citylisten.domain.repository

import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.model.Place

interface PlaceRepository {
    suspend fun findNearby(point: GeoPoint, radiusMeters: Int): List<Place>
    suspend fun enrich(place: Place): Place
}