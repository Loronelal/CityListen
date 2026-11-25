package com.citylisten.data.remote.mapper

import com.citylisten.data.remote.dto.NominatimResponseDto
import com.citylisten.domain.model.Address

fun NominatimResponseDto.toDomain(): Address? {
    val name = displayName.trim()
    if (name.isEmpty()) return null

    val dto = address
    val road = dto?.road?.trim()?.takeIf { it.isNotEmpty() }
        ?: dto?.pedestrian?.trim()?.takeIf { it.isNotEmpty() }
    val city = listOfNotNull(dto?.city, dto?.town, dto?.village, dto?.municipality)
        .map { it.trim() }
        .firstOrNull { it.isNotEmpty() }

    return Address(
        displayName = name,
        road = road,
        houseNumber = dto?.houseNumber?.trim()?.takeIf { it.isNotEmpty() },
        city = city
    )
}
