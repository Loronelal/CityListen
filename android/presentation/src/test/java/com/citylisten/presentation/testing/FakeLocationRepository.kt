package com.citylisten.presentation.testing

import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

class FakeLocationRepository(
    private val initialPoints: List<GeoPoint> = emptyList(),
    private val failure: Throwable? = null,
    private val available: Boolean = true
) : LocationRepository {

    private val updates = MutableSharedFlow<GeoPoint>(extraBufferCapacity = 8)

    override fun isLocationAvailable(): Boolean = available

    override fun observeLocation(): Flow<GeoPoint> = flow {
        failure?.let { throw it }
        initialPoints.forEach { emit(it) }
        emitAll(updates)
    }

    suspend fun emit(point: GeoPoint) {
        updates.emit(point)
    }
}
