package com.project.whobusy.presentation

import androidx.navigation3.runtime.NavKey
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.presentation.navigation.ScreenTypes
import com.project.whobusy.presentation.navigation.detailsRoute
import com.project.whobusy.presentation.navigation.navigateTo
import com.project.whobusy.presentation.navigation.navigateToTab
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ScreenTypesTest {
    @Test
    fun bottomBarVisibilityIsDefinedByScreenType() {
        val withBar: List<ScreenTypes> = listOf(ScreenTypes.Calendar, ScreenTypes.Calendars, ScreenTypes.Profile)
        val withoutBar: List<ScreenTypes> =
            listOf(
                ScreenTypes.Login,
                ScreenTypes.EventDetails(17),
                ScreenTypes.EventDetails(17, "2026-10-24T10:30"),
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
                ScreenTypes.EventDetails(17, "2026-10-24T10:30"),
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

    @Test
    fun eventDetailsRouteRetainsIdAndSelectedOccurrenceStart() {
        val event = recurringEvent()

        val route = event.detailsRoute()

        assertEquals(event.id, route.eventId)
        assertEquals(event.start.toString(), route.occurrenceStart)
        assertEquals(event.start, LocalDateTime.parse(route.occurrenceStart))
    }

    @Test
    fun differentOccurrencesOfOneSeriesHaveDifferentDetailsRoutes() {
        val first = recurringEvent()
        val second = first.copy(start = first.start.plusWeeks(1), end = first.end.plusWeeks(1))
        val backStack = mutableListOf<NavKey>(ScreenTypes.Calendar)

        backStack.navigateTo(first.detailsRoute())
        backStack.navigateTo(second.detailsRoute())

        assertEquals(first.id, second.id)
        assertNotEquals(first.detailsRoute(), second.detailsRoute())
        assertEquals(listOf(ScreenTypes.Calendar, first.detailsRoute(), second.detailsRoute()), backStack)
    }

    private fun recurringEvent(): CalendarEvent {
        val start = LocalDateTime.of(2026, 10, 17, 10, 30)
        return CalendarEvent(
            id = 17,
            ownerId = 1,
            ownerName = "Александр",
            title = "Еженедельная встреча",
            description = "",
            location = "",
            start = start,
            end = start.plusHours(1),
            allDay = false,
            status = EventStatus.BUSY,
            displayColor = "#2196F3",
            recurrenceRule = "FREQ=WEEKLY",
            seriesStart = start,
            seriesEnd = start.plusHours(1),
        )
    }
}
