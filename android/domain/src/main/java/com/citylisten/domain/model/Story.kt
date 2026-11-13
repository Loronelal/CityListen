package com.citylisten.domain.model

import java.time.Instant

data class Story(
    val id: String,
    val placeId: String,
    val title: String,
    val text: String,
    val language: String,
    val createdAt: Instant,
    val cached: Boolean = false
)