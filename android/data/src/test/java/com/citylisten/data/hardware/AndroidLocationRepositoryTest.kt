package com.citylisten.data.hardware

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Looper
import com.citylisten.domain.model.GeoPoint
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.tasks.Task
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AndroidLocationRepositoryTest {

    @Test
    fun `emits point from location callback`() = runTest {
        mockkStatic(Looper::class)
        every { Looper.getMainLooper() } returns mockk(relaxed = true)

        val context = mockk<Context>()
        every {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED
        every {
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_GRANTED

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
    fun `fails without location permission`() = runTest {
        val context = mockk<Context>()
        every {
            context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        every {
            context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
        } returns PackageManager.PERMISSION_DENIED
        val client = mockk<FusedLocationProviderClient>(relaxed = true)

        val repository = AndroidLocationRepository(context, client)
        val result = runCatching { repository.observeLocation().first() }

        assertTrue(result.exceptionOrNull() is SecurityException)
    }
}
