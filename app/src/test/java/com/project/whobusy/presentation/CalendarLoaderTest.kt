package com.project.whobusy.presentation

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.presentation.calendar.viewmodel.CalendarLoader
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class CalendarLoaderTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)

    @Test
    fun requestsFullPreviousCurrentAndNextMonthGridsAcrossYearAndLeapMonthBoundaries() =
        runTest {
            val fixture = fixture()

            val january = fixture.loader.load(LocalDate.parse("2028-01-15"))
            val march = fixture.loader.load(LocalDate.parse("2024-03-15"))

            assertEquals(
                listOf(
                    CalendarQuery(
                        LocalDate.parse("2027-11-29").atStartOfDay(),
                        LocalDate.parse("2028-03-06").atStartOfDay(),
                    ),
                    CalendarQuery(
                        LocalDate.parse("2024-01-29").atStartOfDay(),
                        LocalDate.parse("2024-05-06").atStartOfDay(),
                    ),
                ),
                fixture.queries,
            )
            assertEquals(LocalDate.parse("2027-11-29"), january.loadedStart)
            assertEquals(LocalDate.parse("2028-03-06"), january.loadedEndExclusive)
            assertEquals(LocalDate.parse("2024-01-29"), march.loadedStart)
            assertEquals(LocalDate.parse("2024-05-06"), march.loadedEndExclusive)
            assertFalse(january.isLoading)
            assertFalse(march.isLoading)
        }

    @Test
    fun expandsRecurringOccurrencesThroughoutTheWholeWindowAndKeepsTheOriginalSeries() =
        runTest {
            val original = event(1, LocalDate.parse("2026-08-01")).copy(recurrenceRule = "FREQ=DAILY")
            val fixture = fixture(listOf(original))

            val state = fixture.loader.load(LocalDate.parse("2026-10-17"))
            val start = requireNotNull(state.loadedStart)
            val end = requireNotNull(state.loadedEndExclusive)

            assertEquals(listOf(original), state.series)
            assertEquals(ChronoUnit.DAYS.between(start, end).toInt(), state.events.size)
            assertEquals(start, state.events.first().start.toLocalDate())
            assertEquals(end.minusDays(1), state.events.last().start.toLocalDate())
            assertTrue(state.events.all { it.seriesStart == original.start && it.seriesEnd == original.end })
        }

    @Test
    fun anEmptySuccessfulWindowStillPublishesCoverageAndNonRecurringEventsUseExclusiveEnd() =
        runTest {
            val empty = fixture().loader.load(LocalDate.parse("2026-10-17"))
            val start = requireNotNull(empty.loadedStart)
            val end = requireNotNull(empty.loadedEndExclusive)
            assertTrue(empty.events.isEmpty())
            assertFalse(empty.isLoading)

            val state = fixture(
                listOf(
                    event(1, start.minusDays(1)),
                    event(2, start),
                    event(3, end.minusDays(1)),
                    event(4, end),
                ),
            ).loader.load(LocalDate.parse("2026-10-17"))

            assertEquals(listOf(2, 3), state.events.map { it.id })
            assertEquals(start, state.loadedStart)
            assertEquals(end, state.loadedEndExclusive)
        }

    private suspend fun fixture(events: List<CalendarEvent> = emptyList()): Fixture {
        val session = SessionStore()
        val accounts = MockAccountStore()
        val auth = MockAuthRepository(session, accounts)
        auth.login("demo", DemoAccount.PASSWORD).getOrThrow()
        val workspace = MockWorkspaceRepository(session, accounts, clock)
        val delegate = MockCalendarRepository(session, clock, workspace, accounts)
        val queries = mutableListOf<CalendarQuery>()
        val repository = object : CalendarRepository by delegate {
            override suspend fun getEvents(query: CalendarQuery): Result<List<CalendarEvent>> {
                queries += query
                return Result.success(events)
            }
        }
        return Fixture(CalendarLoader(repository, auth, workspace), queries)
    }

    private fun event(id: Int, date: LocalDate) =
        CalendarEvent(
            id = id,
            ownerId = DemoAccount.user.id,
            ownerName = DemoAccount.user.fullName,
            title = "Событие $id",
            description = "",
            location = "",
            start = date.atTime(10, 0),
            end = date.atTime(11, 0),
            allDay = false,
            status = EventStatus.BUSY,
            displayColor = DemoAccount.user.color,
        )

    private data class Fixture(
        val loader: CalendarLoader,
        val queries: MutableList<CalendarQuery>,
    )
}
