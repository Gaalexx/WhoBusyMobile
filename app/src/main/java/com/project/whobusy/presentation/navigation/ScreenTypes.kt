package com.project.whobusy.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed class ScreenTypes : NavKey {
    sealed interface BottomBarNavigatable

    @Serializable
    data object Login : ScreenTypes()

    @Serializable
    data object Registration : ScreenTypes()

    @Serializable
    data object Calendar : ScreenTypes(), BottomBarNavigatable

    @Serializable
    data object Calendars : ScreenTypes(), BottomBarNavigatable

    @Serializable
    data object Profile : ScreenTypes(), BottomBarNavigatable

    @Serializable
    data object ProfileEditor : ScreenTypes()

    @Serializable
    data object BoardSettings : ScreenTypes()

    @Serializable
    data object CreateSpace : ScreenTypes()

    @Serializable
    data object JoinSpace : ScreenTypes()

    @Serializable
    data object Invitations : ScreenTypes()

    @Serializable
    data class SpaceDetails(
        val spaceId: Int,
    ) : ScreenTypes()

    @Serializable
    data class SpaceMembers(
        val spaceId: Int,
    ) : ScreenTypes()

    @Serializable
    data class MemberRole(
        val spaceId: Int,
        val userId: Int,
    ) : ScreenTypes()

    @Serializable
    data class InviteMember(
        val spaceId: Int,
    ) : ScreenTypes()

    @Serializable
    data class SpaceSettings(
        val spaceId: Int,
    ) : ScreenTypes()

    @Serializable
    data class LeaveSpace(
        val spaceId: Int,
    ) : ScreenTypes()

    @Serializable
    data object EventVisibility : ScreenTypes()

    @Serializable
    data class SpaceVisibility(
        val spaceId: Int,
    ) : ScreenTypes()

    @Serializable
    data object EventDates : ScreenTypes()

    @Serializable
    data object Users : ScreenTypes()

    @Serializable
    data class UserEditor(
        val userId: Int? = null,
    ) : ScreenTypes()

    @Serializable
    data class EventDetails(
        val eventId: Int,
    ) : ScreenTypes()

    @Serializable
    data class EventEditor(
        val date: String,
        val eventId: Int? = null,
        val spaceId: Int? = null,
    ) : ScreenTypes()
}
