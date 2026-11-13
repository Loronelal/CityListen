package com.citylisten.domain.repository

import com.citylisten.domain.model.GeoPoint
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun observeLocation(): Flow<GeoPoint>
}