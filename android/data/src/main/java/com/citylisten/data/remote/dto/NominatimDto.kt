package com.citylisten.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NominatimResponseDto(
    @SerialName("display_name") val displayName: String = "",
    val address: NominatimAddressDto? = null,
    val error: String? = null
)

@Serializable
data class NominatimAddressDto(
    val road: String? = null,
    val pedestrian: String? = null,
    @SerialName("house_number") val houseNumber: String? = null,
    val city: String? = null,
    val town: String? = null,
    val village: String? = null,
    val municipality: String? = null
)
