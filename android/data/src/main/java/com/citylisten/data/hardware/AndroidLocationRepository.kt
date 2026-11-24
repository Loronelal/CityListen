package com.citylisten.data.hardware

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.repository.LocationRepository
import com.citylisten.domain.result.LocationUnavailableException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidLocationRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val client: FusedLocationProviderClient
) : LocationRepository {

    override fun isLocationAvailable(): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    override fun observeLocation(): Flow<GeoPoint> = callbackFlow {
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let { location ->
                    trySend(GeoPoint(location.latitude, location.longitude))
                }
            }
        }

        when {
            !context.hasLocationPermission() ->
                close(SecurityException("Location permission is not granted"))
            !isLocationAvailable() ->
                close(LocationUnavailableException())
            else -> {
                val request = LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    UPDATE_INTERVAL_MILLIS
                )
                    .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MILLIS)
                    .setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_METERS)
                    .build()

                try {
                    client.requestLocationUpdates(request, callback, Looper.getMainLooper())
                        .addOnFailureListener { error -> close(error) }
                } catch (error: SecurityException) {
                    close(error)
                }
            }
        }

        awaitClose { client.removeLocationUpdates(callback) }
    }

    private fun Context.hasLocationPermission(): Boolean =
        checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    companion object {
        const val UPDATE_INTERVAL_MILLIS = 5_000L
        const val MIN_UPDATE_INTERVAL_MILLIS = 2_000L
        const val MIN_UPDATE_DISTANCE_METERS = 10f
    }
}
