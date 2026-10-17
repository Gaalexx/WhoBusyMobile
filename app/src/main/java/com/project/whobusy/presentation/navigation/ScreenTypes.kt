package com.project.whobusy.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenTypes : NavKey {
    sealed interface BottomBarNavigatable

    @Serializable
    data object Login : ScreenTypes()

    @Serializable
    data object Calendar : ScreenTypes(), BottomBarNavigatable

    @Serializable
    data object Calendars : ScreenTypes(), BottomBarNavigatable

    @Serializable
    data object Profile : ScreenTypes(), BottomBarNavigatable

    @Serializable
    data class EventDetails(
        val eventId: Int,
    ) : ScreenTypes()

    @Serializable
    data class EventEditor(
        val date: String,
        val eventId: Int? = null,
    ) : ScreenTypes()
}
