@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.citylisten.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthResponseDto(
    val token: String,
    val userId: String,
    val expiresIn: Long
)