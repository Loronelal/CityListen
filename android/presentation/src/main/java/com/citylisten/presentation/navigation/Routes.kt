package com.citylisten.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object OnboardingRoute

@Serializable
data object WalkRoute

@Serializable
data object HistoryRoute

@Serializable
data object MapRoute

@Serializable
data object SettingsRoute

@Serializable
data object ProfileRoute

@Serializable
data object PermissionRoute

@Serializable
data object PermissionDeniedRoute

@Serializable
data class StoryRoute(val storyId: String)
