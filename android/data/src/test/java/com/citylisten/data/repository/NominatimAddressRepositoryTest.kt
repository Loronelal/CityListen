package com.citylisten.data.repository

import com.citylisten.data.remote.api.NominatimApi
import com.citylisten.data.remote.dto.NominatimAddressDto
import com.citylisten.data.remote.dto.NominatimResponseDto
import com.citylisten.domain.model.GeoPoint
import com.citylisten.domain.result.DomainErrorType
import com.citylisten.domain.result.DomainResult
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class NominatimAddressRepositoryTest {

    private val api: NominatimApi = mockk()
    private val repository = NominatimAddressRepository(api)
    private val point = GeoPoint(55.7558, 37.6173)

    @Test
    fun `returns address for successful response`() = runTest {
        coEvery {
            api.reverse(point.latitude, point.longitude, "ru")
        } returns NominatimResponseDto(
            displayName = "Тверская улица, 1, Москва, Россия",
            address = NominatimAddressDto(
                road = "Тверская улица",
                houseNumber = "1",
                city = "Москва"
            )
        )

        val result = repository.reverseGeocode(point, "ru")

        assertTrue(result is DomainResult.Success)
        val address = (result as DomainResult.Success).data
        assertEquals("Тверская улица, 1, Москва, Россия", address.displayName)
        assertEquals("Тверская улица, 1, Москва", address.shortName)
    }

    @Test
    fun `returns failure for blank address`() = runTest {
        coEvery {
            api.reverse(point.latitude, point.longitude, any())
        } returns NominatimResponseDto(displayName = "  ")

        val result = repository.reverseGeocode(point, "ru")

        assertTrue(result is DomainResult.Failure)
        assertEquals(
            DomainErrorType.UNKNOWN,
            (result as DomainResult.Failure).error.type
        )
    }

    @Test
    fun `maps missing network to network error`() = runTest {
        coEvery {
            api.reverse(point.latitude, point.longitude, any())
        } throws IOException("Нет сети")

        val result = repository.reverseGeocode(point, "ru")

        assertTrue(result is DomainResult.Failure)
        assertEquals(
            DomainErrorType.NETWORK,
            (result as DomainResult.Failure).error.type
        )
    }

    @Test
    fun `maps timeout to timeout error`() = runTest {
        coEvery {
            api.reverse(point.latitude, point.longitude, any())
        } throws SocketTimeoutException("timeout")

        val result = repository.reverseGeocode(point, "ru")

        assertTrue(result is DomainResult.Failure)
        assertEquals(
            DomainErrorType.TIMEOUT,
            (result as DomainResult.Failure).error.type
        )
    }

    @Test
    fun `maps http 5xx to server error`() = runTest {
        coEvery {
            api.reverse(point.latitude, point.longitude, any())
        } throws httpException(503)

        val result = repository.reverseGeocode(point, "ru")

        assertTrue(result is DomainResult.Failure)
        assertEquals(
            DomainErrorType.SERVER,
            (result as DomainResult.Failure).error.type
        )
    }

    @Test
    fun `maps http 4xx to unknown error`() = runTest {
        coEvery {
            api.reverse(point.latitude, point.longitude, any())
        } throws httpException(404)

        val result = repository.reverseGeocode(point, "ru")

        assertTrue(result is DomainResult.Failure)
        assertEquals(
            DomainErrorType.UNKNOWN,
            (result as DomainResult.Failure).error.type
        )
    }

    private fun httpException(code: Int): HttpException = HttpException(
        Response.error<Any>(
            code,
            """{"error":"fail"}""".toResponseBody("application/json".toMediaType())
        )
    )
}
