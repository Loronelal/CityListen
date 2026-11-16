@file:OptIn(kotlinx.serialization.InternalSerializationApi::class)
package com.citylisten.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class StoryDto(
    val id: String,
    val placeId: String,
    val title: String,
    val text: String,
    val language: String,
    val createdAt: String
)

@Serializable
data class GenerateStoryRequest(
    val placeId: String,
    val language: String
)