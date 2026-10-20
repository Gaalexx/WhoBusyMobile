package com.project.whobusy.data

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.domain.model.SpaceRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class MockSpaceCalendarRepositoryTest {
    private val session = SessionStore()
    private val accounts = MockAccountStore()
    private val clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)
    private val workspace = MockWorkspaceRepository(session, accounts, clock)
    private val auth = MockAuthRepository(session, accounts)
    private val calendar = MockCalendarRepository(session, clock, workspace, accounts)
    private val today = LocalDate.of(2026, 10, 17)

    @Test
    fun visitorDoesNotReceiveActorsPlansAndBusyPlansHaveNoPrivateDetails() =
        runBlocking {
            login("daria")
            val events = calendar.getEvents(query(setOf(1))).getOrThrow()

            assertFalse(events.any { it.id == 2 })
            val busy = events.single { it.id == 3 }
            assertTrue(busy.busyOnly)
            assertEquals("Занято", busy.title)
            assertEquals("", busy.description)
            assertEquals("", busy.location)
            assertNull(busy.color)
            assertEquals(
                accounts.users.value
                    .single { it.id == busy.ownerId }
                    .color,
                busy.displayColor,
            )
            assertTrue(busy.shares.isEmpty())
            assertFalse(busy.canEdit)
            assertFalse(events.single { it.id == 1 }.busyOnly)
        }

    @Test
    fun actorSeesSharedDetailsButOnlyOwnerReceivesShareSettings() =
        runBlocking {
            login("mikhail")
            val events = calendar.getEvents(query(setOf(1))).getOrThrow()
            val shared = events.single { it.id == 2 }

            assertFalse(shared.busyOnly)
            assertEquals("Главный экран и навигация", shared.description)
            assertEquals("Удалённо", shared.location)
            assertTrue(shared.shares.isEmpty())
            assertFalse(shared.canEdit)
            val own = events.single { it.id == 3 }
            assertFalse(own.busyOnly)
            assertTrue(own.canEdit)
            assertEquals(listOf(EventShare(1, EventVisibility.Busy)), own.shares)
        }

    @Test
    fun combinedSpacesChooseFullDetailsOverBusyMaskAndDoNotDuplicateThePlan() =
        runBlocking {
            login("demo")
            workspace.updateMemberRole(1, 4, SpaceRole.Actor).getOrThrow()
            login("daria")
            assertTrue(
                calendar
                    .getEvents(query(setOf(2)))
                    .getOrThrow()
                    .single { it.id == 2 }
                    .busyOnly,
            )

            val combined = calendar.getEvents(query(setOf(1, 2))).getOrThrow().filter { it.id == 2 }

            assertEquals(1, combined.size)
            assertFalse(combined.single().busyOnly)
            assertEquals("Главный экран и навигация", combined.single().description)
        }

    @Test
    fun globalAdminDoesNotGainMembershipOrReadAndWriteAccessToOthersPlans() =
        runBlocking {
            login("admin", "admin123")
            assertTrue(calendar.getEvents(query(setOf(1, 2, 3, 4))).getOrThrow().isEmpty())
            assertTrue(calendar.deleteEvent(1).isFailure)
            assertTrue(calendar.saveEvent(draft(spaceId = 1), null).isFailure)

            val personal = calendar.saveEvent(draft(), null).getOrThrow()
            val own = calendar.getEvents(query(emptySet(), showOwn = true)).getOrThrow()

            assertEquals(listOf(personal.id), own.map { it.id })
            assertTrue(own.single().canEdit)
        }

    @Test
    fun spaceModeratorCanEditAndDeleteSpacePlansButCannotEditPersonalShares() =
        runBlocking {
            login("demo")
            val personal =
                calendar
                    .saveEvent(draft(shares = listOf(EventShare(1, EventVisibility.Actors))), null)
                    .getOrThrow()
            val public = calendar.saveEvent(draft(spaceId = 1), null).getOrThrow()
            login("anna")
            val visible = calendar.getEvents(query(setOf(1))).getOrThrow()

            assertFalse(visible.single { it.id == personal.id }.canEdit)
            assertTrue(visible.single { it.id == public.id }.canEdit)
            assertTrue(calendar.saveEvent(draft(title = "Чужой личный план"), personal.id).isFailure)

            val edited = calendar.saveEvent(draft(title = "Изменён модератором", spaceId = 1), public.id).getOrThrow()
            assertEquals(1, edited.ownerId)
            assertEquals("Изменён модератором", edited.title)
            calendar.deleteEvent(public.id).getOrThrow()
            assertFalse(calendar.getEvents(query(setOf(1))).getOrThrow().any { it.id == public.id })
        }

    @Test
    fun placementCannotChangeAndInvalidShareListsDoNotMutateTheOriginal() =
        runBlocking {
            login("demo")
            val original = calendar.saveEvent(draft(spaceId = 1), null).getOrThrow()

            assertTrue(calendar.saveEvent(draft(spaceId = 2), original.id).isFailure)
            assertTrue(
                calendar
                    .saveEvent(
                        draft(spaceId = 1, shares = listOf(EventShare(2, EventVisibility.Busy))),
                        null,
                    ).isFailure,
            )
            assertTrue(
                calendar
                    .saveEvent(
                        draft(
                            shares = listOf(EventShare(1, EventVisibility.Actors), EventShare(1, EventVisibility.Busy)),
                        ),
                        null,
                    ).isFailure,
            )
            assertTrue(calendar.saveEvent(draft(shares = listOf(EventShare(4, EventVisibility.Busy))), null).isFailure)
            assertEquals(
                1,
                calendar
                    .getEvents(query(setOf(1)))
                    .getOrThrow()
                    .single { it.id == original.id }
                    .spaceId,
            )
        }

    @Test
    fun leavingSpaceTurnsOwnSpacePlansPersonalAndRemovesOnlyThatSpaceShare() =
        runBlocking {
            login("anna")
            val public = calendar.saveEvent(draft(spaceId = 1), null).getOrThrow()
            val personal =
                calendar
                    .saveEvent(
                        draft(
                            shares = listOf(EventShare(1, EventVisibility.Actors), EventShare(2, EventVisibility.Busy)),
                        ),
                        null,
                    ).getOrThrow()

            workspace.leaveSpace(1).getOrThrow()
            val own = calendar.getEvents(query(emptySet(), showOwn = true)).getOrThrow()

            assertNull(own.single { it.id == public.id }.spaceId)
            assertEquals(listOf(EventShare(2, EventVisibility.Busy)), own.single { it.id == personal.id }.shares)
            assertTrue(own.single { it.id == public.id }.canEdit)
            login("demo")
            val formerSpace = calendar.getEvents(query(setOf(1))).getOrThrow()
            assertFalse(formerSpace.any { it.id == public.id || it.id == personal.id })
        }

    @Test
    fun deletingSpacePreservesOwnersPlansAndTheirOtherSpaceShares() =
        runBlocking {
            login("demo")
            workspace.deleteSpace(1).getOrThrow()
            val own = calendar.getEvents(query(emptySet(), showOwn = true)).getOrThrow()
            assertNull(own.single { it.id == 1 }.spaceId)
            assertTrue(own.single { it.id == 1 }.canEdit)

            login("anna")
            val shared = calendar.getEvents(query(emptySet(), showOwn = true)).getOrThrow().single { it.id == 2 }
            assertEquals(listOf(EventShare(2, EventVisibility.Busy)), shared.shares)
            assertEquals("Главный экран и навигация", shared.description)
        }

    @Test
    fun deletingAccountRemovesItsPlansFromOtherMembersBoards() =
        runBlocking {
            login("demo")
            assertTrue(calendar.getEvents(query(setOf(1))).getOrThrow().any { it.ownerId == 2 })
            login("admin", "admin123")
            workspace.deleteUser(2).getOrThrow()
            login("demo")

            assertFalse(calendar.getEvents(query(setOf(1, 2))).getOrThrow().any { it.ownerId == 2 })
        }

    private suspend fun login(
        login: String,
        password: String = DemoAccount.PASSWORD,
    ) {
        auth.login(login, password).getOrThrow()
    }

    private fun query(
        spaces: Set<Int>,
        showOwn: Boolean = false,
    ) = CalendarQuery(today.atStartOfDay(), today.plusDays(1).atStartOfDay(), spaceIds = spaces, showOwn = showOwn)

    private fun draft(
        title: String = "Новый план",
        spaceId: Int? = null,
        shares: List<EventShare> = emptyList(),
    ) = EventDraft(
        title = title,
        start = today.atTime(16, 0),
        end = today.atTime(17, 0),
        description = "Личные подробности",
        location = "Переговорная",
        spaceId = spaceId,
        shares = shares,
    )
}
