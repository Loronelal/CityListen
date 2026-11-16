package com.citylisten.data.remote.mapper

import com.citylisten.data.remote.dto.StoryDto
import com.citylisten.domain.model.Story
import java.time.Instant

fun StoryDto.toDomain(): Story = Story(
    id = id,
    placeId = placeId,
    title = title,
    text = text,
    language = language,
    createdAt = try { Instant.parse(createdAt) } catch (e: Exception) { Instant.now() },
    cached = false
)