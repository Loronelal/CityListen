package com.citylisten.domain.model

import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
) {
    fun distanceTo(other: GeoPoint): Double {
        val earthRadius = 6_371_000.0
        val dLat = Math.toRadians(other.latitude - latitude)
        val dLon = Math.toRadians(other.longitude - longitude)
        val lat1 = Math.toRadians(latitude)
        val lat2 = Math.toRadians(other.latitude)
        val h = sin(dLat / 2).let { it * it } +
                cos(lat1) * cos(lat2) * sin(dLon / 2).let { it * it }
        return 2 * earthRadius * atan2(sqrt(h), sqrt(1 - h))
    }
}