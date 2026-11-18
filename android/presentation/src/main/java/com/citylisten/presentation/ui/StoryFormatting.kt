package com.citylisten.presentation.ui

import com.citylisten.domain.model.Story
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val storyTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

internal fun Story.timeLabel(): String =
    createdAt.atZone(ZoneId.systemDefault()).format(storyTimeFormatter)

internal fun Story.sourceLabel(): String = when {
    placeId.startsWith("osm", ignoreCase = true) -> "OSM"
    placeId.startsWith("wd", ignoreCase = true) -> "Wikidata"
    else -> "OSM"
}
