package com.citylisten.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stories")
data class StoryEntity(
    @PrimaryKey val id: String,
    val placeId: String,
    val title: String,
    val text: String,
    val language: String,
    val createdAt: Long,
    val cached: Boolean
)