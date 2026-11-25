package com.citylisten.data.remote.api

import com.citylisten.data.remote.dto.NominatimResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NominatimApi {
    @GET("reverse?format=jsonv2&zoom=18&addressdetails=1")
    suspend fun reverse(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("accept-language") language: String
    ): NominatimResponseDto
}
