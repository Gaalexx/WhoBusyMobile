package com.project.whobusy.data

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.InvalidCredentialsException
import kotlinx.coroutines.flow.first
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

class MockRepositoriesTest {
    private val session = SessionStore()
    private val auth = MockAuthRepository(session)
    private val clock = Clock.fixed(Instant.parse("2026-10-15T09:00:00Z"), ZoneOffset.UTC)
    private val calendar = MockCalendarRepository(session, clock)
    private val today = LocalDate.of(2026, 10, 15)

    @Test
    fun loginNormalizesEmailAndLogoutClearsSession() =
        runBlocking {
            val user = auth.login("  DEMO@WHOBUSY.TEST  ", DemoAccount.PASSWORD).getOrThrow()

            assertEquals(user, auth.currentUser.first())
            auth.logout()
            assertNull(auth.currentUser.first())
            assertNull(session.getToken())
        }

    @Test
    fun invalidPasswordDoesNotCreateSession() =
        runBlocking {
            val result = auth.login(DemoAccount.EMAIL, "incorrect")

            assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
            assertNull(session.getUser())
        }

    @Test
    fun calendarRequiresAuthenticatedUser() =
        runBlocking {
            assertTrue(calendar.getUsers().exceptionOrNull() is AuthenticationException)
            assertTrue(calendar.getEvents(query()).exceptionOrNull() is AuthenticationException)
        }

    @Test
    fun boardFiltersSpacesAndMasksPrivatePlans() =
        runBlocking {
            auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow()

            val events = calendar.getEvents(query().copy(spaceIds = setOf(1, 999), showOwn = false)).getOrThrow()

            assertEquals(setOf(1, 2, 3), events.map { it.ownerId }.toSet())
            assertTrue(events.first { it.id == 3 }.busyOnly)
            assertFalse(events.any { it.ownerId == 999 })
        }

    @Test
    fun boardCanShowOnlyOwnPlansOrNothing() =
        runBlocking {
            auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow()
            assertTrue(calendar.getEvents(query().copy(spaceIds = emptySet(), showOwn = false)).getOrThrow().isEmpty())
            assertEquals(
                setOf(1),
                calendar
                    .getEvents(query().copy(spaceIds = emptySet(), showOwn = true))
                    .getOrThrow()
                    .map { it.ownerId }
                    .toSet(),
            )
        }

    private fun query(ownerIds: Set<Int> = emptySet()) =
        CalendarQuery(
            start = today.atStartOfDay(),
            end = today.plusDays(1).atStartOfDay(),
            ownerIds = ownerIds,
        )
}
