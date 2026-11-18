package com.citylisten.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object WalkRoute

@Serializable
data object HistoryRoute

@Serializable
data object MapRoute

@Serializable
data object SettingsRoute

@Serializable
data class StoryRoute(val storyId: String)
