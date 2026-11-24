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
data class PermissionDeniedRoute(val canAskAgain: Boolean)

@Serializable
data class StoryRoute(val storyId: String)
