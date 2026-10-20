package com.project.whobusy.presentation.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

internal val navigationSavedStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(ScreenTypes.Login::class, ScreenTypes.Login.serializer())
            subclass(ScreenTypes.Registration::class, ScreenTypes.Registration.serializer())
            subclass(ScreenTypes.Calendar::class, ScreenTypes.Calendar.serializer())
            subclass(ScreenTypes.Calendars::class, ScreenTypes.Calendars.serializer())
            subclass(ScreenTypes.Profile::class, ScreenTypes.Profile.serializer())
            subclass(ScreenTypes.ProfileEditor::class, ScreenTypes.ProfileEditor.serializer())
            subclass(ScreenTypes.BoardSettings::class, ScreenTypes.BoardSettings.serializer())
            subclass(ScreenTypes.CreateSpace::class, ScreenTypes.CreateSpace.serializer())
            subclass(ScreenTypes.JoinSpace::class, ScreenTypes.JoinSpace.serializer())
            subclass(ScreenTypes.Invitations::class, ScreenTypes.Invitations.serializer())
            subclass(ScreenTypes.SpaceDetails::class, ScreenTypes.SpaceDetails.serializer())
            subclass(ScreenTypes.SpaceMembers::class, ScreenTypes.SpaceMembers.serializer())
            subclass(ScreenTypes.MemberRole::class, ScreenTypes.MemberRole.serializer())
            subclass(ScreenTypes.InviteMember::class, ScreenTypes.InviteMember.serializer())
            subclass(ScreenTypes.SpaceSettings::class, ScreenTypes.SpaceSettings.serializer())
            subclass(ScreenTypes.LeaveSpace::class, ScreenTypes.LeaveSpace.serializer())
            subclass(ScreenTypes.EventVisibility::class, ScreenTypes.EventVisibility.serializer())
            subclass(ScreenTypes.SpaceVisibility::class, ScreenTypes.SpaceVisibility.serializer())
            subclass(ScreenTypes.EventDates::class, ScreenTypes.EventDates.serializer())
            subclass(ScreenTypes.Users::class, ScreenTypes.Users.serializer())
            subclass(ScreenTypes.UserEditor::class, ScreenTypes.UserEditor.serializer())
            subclass(ScreenTypes.EventDetails::class, ScreenTypes.EventDetails.serializer())
            subclass(ScreenTypes.EventEditor::class, ScreenTypes.EventEditor.serializer())
        }
    }
}
