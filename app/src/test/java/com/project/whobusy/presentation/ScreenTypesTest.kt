package com.project.whobusy.presentation

import androidx.navigation3.runtime.NavKey
import com.project.whobusy.presentation.navigation.ScreenTypes
import com.project.whobusy.presentation.navigation.navigateTo
import com.project.whobusy.presentation.navigation.navigateToTab
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ScreenTypesTest {
    @Test
    fun bottomBarVisibilityIsDefinedByScreenType() {
        val withBar: List<ScreenTypes> = listOf(ScreenTypes.Calendar, ScreenTypes.Calendars, ScreenTypes.Profile)
        val withoutBar: List<ScreenTypes> =
            listOf(
                ScreenTypes.Login,
                ScreenTypes.EventDetails(17),
                ScreenTypes.EventEditor("2026-10-17"),
                ScreenTypes.EventEditor("2026-10-17", 17),
                ScreenTypes.Registration,
                ScreenTypes.BoardSettings,
                ScreenTypes.CreateSpace,
                ScreenTypes.JoinSpace,
                ScreenTypes.Invitations,
                ScreenTypes.SpaceDetails(3),
                ScreenTypes.SpaceMembers(3),
                ScreenTypes.MemberRole(3, 2),
                ScreenTypes.InviteMember(3),
                ScreenTypes.SpaceSettings(3),
                ScreenTypes.LeaveSpace(3),
                ScreenTypes.ProfileEditor,
                ScreenTypes.EventVisibility,
                ScreenTypes.SpaceVisibility(3),
                ScreenTypes.EventDates,
                ScreenTypes.Users,
                ScreenTypes.UserEditor(),
                ScreenTypes.UserEditor(2),
            )
        withBar.forEach { assertTrue(it is ScreenTypes.BottomBarNavigatable) }
        withoutBar.forEach { assertFalse(it is ScreenTypes.BottomBarNavigatable) }
    }

    @Test
    fun everyRouteRetainsItsTypeAndArgumentsWhenSaved() {
        val routes: List<ScreenTypes> =
            listOf(
                ScreenTypes.Login,
                ScreenTypes.Calendar,
                ScreenTypes.Calendars,
                ScreenTypes.Profile,
                ScreenTypes.EventDetails(17),
                ScreenTypes.EventEditor("2026-10-17", 17),
                ScreenTypes.EventEditor("2026-10-18"),
                ScreenTypes.EventEditor("2026-10-18", spaceId = 3),
                ScreenTypes.Registration,
                ScreenTypes.BoardSettings,
                ScreenTypes.CreateSpace,
                ScreenTypes.JoinSpace,
                ScreenTypes.Invitations,
                ScreenTypes.SpaceDetails(3),
                ScreenTypes.SpaceMembers(3),
                ScreenTypes.MemberRole(3, 2),
                ScreenTypes.InviteMember(3),
                ScreenTypes.SpaceSettings(3),
                ScreenTypes.LeaveSpace(3),
                ScreenTypes.ProfileEditor,
                ScreenTypes.EventVisibility,
                ScreenTypes.SpaceVisibility(3),
                ScreenTypes.EventDates,
                ScreenTypes.Users,
                ScreenTypes.UserEditor(),
                ScreenTypes.UserEditor(2),
            )
        routes.forEach { route ->
            assertEquals(route, Json.decodeFromString<ScreenTypes>(Json.encodeToString(route)))
        }
    }

    @Test
    fun switchingTabsDoesNotAccumulateEntriesOrReplaceCalendar() {
        val backStack = mutableListOf<NavKey>(ScreenTypes.Calendar)
        backStack.navigateToTab(ScreenTypes.Calendars)
        backStack.navigateToTab(ScreenTypes.Calendars)
        assertEquals(listOf(ScreenTypes.Calendar, ScreenTypes.Calendars), backStack)
        backStack.navigateToTab(ScreenTypes.Profile)
        backStack.navigateToTab(ScreenTypes.Profile)
        assertEquals(listOf(ScreenTypes.Calendar, ScreenTypes.Profile), backStack)
        backStack.removeLastOrNull()
        assertEquals(listOf(ScreenTypes.Calendar), backStack)
        backStack.navigateToTab(ScreenTypes.Calendars)
        backStack.navigateToTab(ScreenTypes.Calendar)
        assertEquals(listOf(ScreenTypes.Calendar), backStack)
    }

    @Test
    fun repeatedEventActionsKeepOneEntryAndBackReturnsToDetails() {
        val backStack = mutableListOf<NavKey>(ScreenTypes.Calendar)
        val details = ScreenTypes.EventDetails(17)
        val editor = ScreenTypes.EventEditor("2026-10-17", 17)
        backStack.navigateTo(details)
        backStack.navigateTo(details)
        backStack.navigateTo(editor)
        backStack.navigateTo(editor)
        assertEquals(listOf(ScreenTypes.Calendar, details, editor), backStack)
        backStack.removeLastOrNull()
        assertEquals(details, backStack.last())
    }
}
