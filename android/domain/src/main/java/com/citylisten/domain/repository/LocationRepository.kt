package com.citylisten.domain.repository

import com.citylisten.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun isLocationAvailable(): Boolean
    fun observeLocation(): Flow<GeoPoint>
}