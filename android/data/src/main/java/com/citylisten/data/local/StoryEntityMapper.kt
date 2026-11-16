package com.citylisten.data.local

import com.citylisten.domain.model.Story
import java.time.Instant

fun StoryEntity.toDomain(): Story = Story(
    id = id,
    placeId = placeId,
    title = title,
    text = text,
    language = language,
    createdAt = Instant.ofEpochMilli(createdAt),
    cached = cached
)

fun Story.toEntity(): StoryEntity = StoryEntity(
    id = id,
    placeId = placeId,
    title = title,
    text = text,
    language = language,
    createdAt = createdAt.toEpochMilli(),
    cached = cached
)