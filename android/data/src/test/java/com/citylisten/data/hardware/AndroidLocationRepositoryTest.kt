package com.citylisten.data.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.result.LocationUnavailableException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidLocationRepositoryTest {

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `emits point from location callback`() = runTest {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)

        val context = contextWithLocation()
        val task = mockk<Task<Void>>(relaxed = true)
        val callback = slot<LocationCallback>()
        val client = mockk<FusedLocationProviderClient>(relaxed = true)
        every {
            client.requestLocationUpdates(any(), capture(callback), any())
        } returns task
        every { client.removeLocationUpdates(any<LocationCallback>()) } returns task

        val repository = AndroidLocationRepository(context, client)
        val point = async { repository.observeLocation().first() }
        runCurrent()

        val location = mockk<Location> {
            every { latitude } returns 55.75
            every { longitude } returns 37.61
        }
        val result = mockk<LocationResult> {
            every { lastLocation } returns location
        }
        callback.captured.onLocationResult(result)

        assertEquals(GeoPoint(55.75, 37.61), point.await())
        verify { client.removeLocationUpdates(callback.captured) }
    }

    @Test
    fun `reports location available when provider is enabled`() {
        val repository = AndroidLocationRepository(
            context = contextWithLocation(gpsEnabled = true, networkEnabled = false),
            client = mockk(relaxed = true)
        )

        assertTrue(repository.isLocationAvailable())
    }

    @Test
    fun `reports location unavailable when providers are disabled`() {
        val repository = AndroidLocationRepository(
            context = contextWithLocation(gpsEnabled = false, networkEnabled = false),
            client = mockk(relaxed = true)
        )

        assertFalse(repository.isLocationAvailable())
    }

    @Test
    fun `fails without location permission`() = runTest {
        val repository = AndroidLocationRepository(
            context = contextWithLocation(permissionGranted = false),
            client = mockk(relaxed = true)
        )
        val result = runCatching { repository.observeLocation().first() }

        assertTrue(result.exceptionOrNull() is SecurityException)
    }

    @Test
    fun `fails when location is disabled`() = runTest {
        val repository = AndroidLocationRepository(
            context = contextWithLocation(gpsEnabled = false, networkEnabled = false),
            client = mockk(relaxed = true)
        )
        val result = runCatching { repository.observeLocation().first() }

        assertTrue(result.exceptionOrNull() is LocationUnavailableException)
    }

    private fun contextWithLocation(
        permissionGranted: Boolean = true,
        gpsEnabled: Boolean = true,
        networkEnabled: Boolean = true
    ): Context {
        val context = mockk<Context>()
        val permission = if (permissionGranted) {
            PackageManager.PERMISSION_GRANTED
        } else {
            PackageManager.PERMISSION_DENIED
        }
        every {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        } returns permission
        every {
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns permission

        val manager = mockk<LocationManager>()
        every { context.getSystemService(Context.LOCATION_SERVICE) } returns manager
        every {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } returns gpsEnabled
        every {
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } returns networkEnabled

        return context
    }
}
