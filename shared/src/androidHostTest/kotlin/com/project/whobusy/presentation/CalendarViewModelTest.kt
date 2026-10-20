package com.project.whobusy.presentation

import androidx.lifecycle.SavedStateHandle
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.model.monthDates
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarEffect
import com.project.whobusy.presentation.calendar.viewmodel.CalendarLoader
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import com.project.whobusy.time.Clock
import com.project.whobusy.time.Instant
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.YearMonth
import com.project.whobusy.time.ZoneOffset
import com.project.whobusy.time.atTime
import com.project.whobusy.time.from
import com.project.whobusy.time.of
import com.project.whobusy.time.parse
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.toLocalDate
import com.project.whobusy.time.value
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)
    private val draft =
        EventDraft(
            title = "Обсуждение проекта",
            start = LocalDate.of(2026, 10, 20).atTime(10, 0),
            end = LocalDate.of(2026, 10, 20).atTime(11, 0),
        )

    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun cachesDistinctOccurrencesWithoutChangingCalendarState() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()
            val state = viewModel.state.value
            val first = draft.toEvent(42).copy(
                recurrenceRule = "FREQ=DAILY",
                seriesStart = draft.start,
                seriesEnd = draft.end,
            )
            val second = first.copy(start = first.start.plusDays(1), end = first.end.plusDays(1))

            viewModel.rememberEvent(first)
            viewModel.rememberEvent(second)

            assertEquals(first, viewModel.event(first.id, first.start.toString()))
            assertEquals(second, viewModel.event(second.id, second.start.toString()))
            assertEquals(second, viewModel.event(second.id))
            assertNull(viewModel.event(first.id, first.start.plusDays(2).toString()))
            assertEquals(first.start, viewModel.series(first.id)?.start)
            assertEquals(first.end, viewModel.series(first.id)?.end)
            assertEquals(state, viewModel.state.value)
            assertNull(viewModel.state.value.result)
        }

    @Test
    fun saveCompletionPersistsThroughReloadAndBlocksMutationsUntilHandled() =
        runTest {
            val repository = FakeCalendarRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.onEvent(CalendarCommand.SaveEvent(draft, eventId = 42))
            assertTrue(viewModel.state.value.isSaving)
            assertNull(viewModel.state.value.result)
            advanceUntilIdle()
            assertEquals(CalendarEffect.EventSaved(42), viewModel.state.first().result)
            assertEquals(listOf(42), repository.savedEventIds)
            assertEquals(2, repository.eventLoadCount)

            viewModel.onEvent(CalendarCommand.Refresh)
            advanceUntilIdle()
            assertEquals(CalendarEffect.EventSaved(42), viewModel.state.value.result)
            viewModel.onEvent(CalendarCommand.SaveEvent(draft))
            viewModel.onEvent(CalendarCommand.DeleteEvent(42))
            advanceUntilIdle()
            assertEquals(listOf(42), repository.savedEventIds)
            assertTrue(repository.deletedEventIds.isEmpty())
            assertEquals(CalendarEffect.EventSaved(42), viewModel.state.value.result)

            viewModel.onEvent(CalendarCommand.ResultHandled)
            assertNull(viewModel.state.value.result)
            viewModel.onEvent(CalendarCommand.SaveEvent(draft))
            advanceUntilIdle()

            assertEquals(listOf(42, null), repository.savedEventIds)
            assertEquals(CalendarEffect.EventSaved(null), viewModel.state.value.result)
            assertEquals(4, repository.eventLoadCount)
            assertFalse(viewModel.state.value.isSaving)
            assertNull(viewModel.state.value.error)
        }

    @Test
    fun deleteUsesExplicitEventIdAndRetainsCompletionUntilHandled() =
        runTest {
            val repository = FakeCalendarRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.onEvent(CalendarCommand.DeleteEvent(42))
            advanceUntilIdle()

            assertEquals(listOf(42), repository.deletedEventIds)
            assertEquals(CalendarEffect.EventDeleted(42), viewModel.state.first().result)
            assertEquals(2, repository.eventLoadCount)
            assertFalse(viewModel.state.value.isSaving)

            viewModel.onEvent(CalendarCommand.DeleteEvent(43))
            advanceUntilIdle()
            assertEquals(listOf(42), repository.deletedEventIds)
            assertEquals(CalendarEffect.EventDeleted(42), viewModel.state.value.result)

            viewModel.onEvent(CalendarCommand.ResultHandled)
            assertNull(viewModel.state.value.result)
            viewModel.onEvent(CalendarCommand.DeleteEvent(43))
            advanceUntilIdle()
            assertEquals(listOf(42, 43), repository.deletedEventIds)
            assertEquals(CalendarEffect.EventDeleted(43), viewModel.state.value.result)
            assertEquals(3, repository.eventLoadCount)
        }

    @Test
    fun acknowledgementDuringRefreshDoesNotRestoreTheHandledCompletion() =
        runTest {
            val repository = FakeCalendarRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()
            viewModel.onEvent(CalendarCommand.SaveEvent(draft, eventId = 42))
            advanceUntilIdle()
            val pendingLoad = CompletableDeferred<Unit>()
            repository.pendingLoad = pendingLoad

            viewModel.onEvent(CalendarCommand.Refresh)
            runCurrent()
            assertTrue(viewModel.state.value.isLoading)
            assertEquals(CalendarEffect.EventSaved(42), viewModel.state.value.result)
            viewModel.onEvent(CalendarCommand.ResultHandled)
            assertNull(viewModel.state.value.result)

            pendingLoad.complete(Unit)
            advanceUntilIdle()
            assertFalse(viewModel.state.value.isLoading)
            assertNull(viewModel.state.value.result)
        }

    @Test
    fun failedMutationsKeepErrorWithoutCompletionAndAllowRetry() =
        runTest {
            val repository = FakeCalendarRepository().apply { mutationError = IllegalStateException("Unavailable") }
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            viewModel.onEvent(CalendarCommand.SaveEvent(draft, eventId = 42))
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.error)
            assertNull(viewModel.state.value.result)
            assertFalse(viewModel.state.value.isSaving)
            viewModel.onEvent(CalendarCommand.ClearError)
            assertNull(viewModel.state.value.error)
            viewModel.onEvent(CalendarCommand.DeleteEvent(42))
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.error)
            assertFalse(viewModel.state.value.isSaving)
            assertFalse(viewModel.state.value.isLoading)
            assertEquals(1, repository.eventLoadCount)
            assertNull(viewModel.state.value.result)

            repository.mutationError = null
            viewModel.onEvent(CalendarCommand.SaveEvent(draft, eventId = 42))
            advanceUntilIdle()
            assertEquals(CalendarEffect.EventSaved(42), viewModel.state.value.result)
            assertNull(viewModel.state.value.error)
        }

    @Test
    fun restoresAndSavesDateAndModeWithoutStoringNavigation() =
        runTest {
            val savedState = SavedStateHandle(mapOf("date" to "2026-10-20", "mode" to CalendarMode.Week.name))
            val viewModel = createViewModel(savedState = savedState)
            advanceUntilIdle()
            assertEquals(LocalDate.of(2026, 10, 20), viewModel.state.value.selectedDate)
            assertEquals(CalendarMode.Week, viewModel.state.value.mode)

            val selectedDate = LocalDate.of(2026, 11, 4)
            viewModel.onEvent(CalendarCommand.SelectDate(selectedDate))
            viewModel.onEvent(CalendarCommand.SelectMode(CalendarMode.Month))
            advanceUntilIdle()

            assertEquals(selectedDate, viewModel.state.value.selectedDate)
            assertEquals(CalendarMode.Month, viewModel.state.value.mode)
            assertEquals("2026-11-04", savedState.get<String>("date"))
            assertEquals(CalendarMode.Month.name, savedState.get<String>("mode"))
            assertEquals(setOf("date", "mode"), savedState.keys())
            assertNull(viewModel.state.value.result)

            val restored = createViewModel(savedState = savedState)
            advanceUntilIdle()
            assertEquals(selectedDate, restored.state.value.selectedDate)
            assertEquals(CalendarMode.Month, restored.state.value.mode)
        }

    @Test
    fun datesInTheThreeCoveredMonthsReuseTheLoadedWindowWithoutLoading() =
        runTest {
            val repository = FakeCalendarRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()

            listOf("2026-09-01", "2026-10-31", "2026-11-30").forEach { value ->
                val date = LocalDate.parse(value)
                viewModel.onEvent(CalendarCommand.SelectDate(date))
                assertEquals(date, viewModel.state.value.selectedDate)
                assertFalse(viewModel.state.value.isLoading)
                advanceUntilIdle()
            }

            assertEquals(1, repository.eventLoadCount)
            assertEquals(LocalDate.parse("2026-08-31"), viewModel.state.value.loadedStart)
            assertEquals(LocalDate.parse("2026-12-07"), viewModel.state.value.loadedEndExclusive)
        }

    @Test
    fun aSpilloverDayLoadsTheFullSelectedMonthInsteadOfTreatingMissingEventsAsEmpty() =
        runTest {
            val repository = FakeCalendarRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()
            val date = LocalDate.parse("2026-12-01")
            assertTrue(date < requireNotNull(viewModel.state.value.loadedEndExclusive))

            viewModel.onEvent(CalendarCommand.SelectDate(date))
            assertTrue(viewModel.state.value.isLoading)
            advanceUntilIdle()

            assertEquals(2, repository.eventLoadCount)
            assertEquals(date, viewModel.state.value.selectedDate)
            val month = monthDates(YearMonth.from(date))
            assertTrue(month.first() >= requireNotNull(viewModel.state.value.loadedStart))
            assertTrue(month.last() < requireNotNull(viewModel.state.value.loadedEndExclusive))
        }

    @Test
    fun selectingInsideAnInFlightWindowPreservesTheLatestDateAndModeWithoutAnotherRequest() =
        runTest {
            val gate = CompletableDeferred<Unit>()
            val repository = FakeCalendarRepository().apply { pendingLoad = gate }
            val savedState = SavedStateHandle()
            val viewModel = createViewModel(repository, savedState)
            runCurrent()
            val latest = LocalDate.parse("2026-11-20")

            viewModel.onEvent(CalendarCommand.SelectDate(LocalDate.parse("2026-09-10")))
            viewModel.onEvent(CalendarCommand.SelectDate(latest))
            viewModel.onEvent(CalendarCommand.SelectMode(CalendarMode.Week))
            runCurrent()
            assertEquals(1, repository.eventLoadCount)
            assertTrue(viewModel.state.value.isLoading)

            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(latest, viewModel.state.value.selectedDate)
            assertEquals(CalendarMode.Week, viewModel.state.value.mode)
            assertEquals(latest.toString(), savedState.get<String>("date"))
            assertFalse(viewModel.state.value.isLoading)
            assertEquals(1, repository.eventLoadCount)
        }

    @Test
    fun aCancelledOldRequestCannotReplaceTheLatestWindowEvenIfItReturnsSuccessfully() =
        runTest {
            val oldGate = CompletableDeferred<Unit>()
            val repository = FakeCalendarRepository().apply {
                loadEvents = { query ->
                    if (query.start.toLocalDate() == LocalDate.parse("2026-08-31")) {
                        withContext(NonCancellable) { oldGate.await() }
                    }
                    listOf(draft.toEvent(query.start.month.ordinal + 1))
                }
            }
            val viewModel = createViewModel(repository)
            runCurrent()
            val latest = LocalDate.parse("2027-04-15")

            viewModel.onEvent(CalendarCommand.SelectDate(latest))
            runCurrent()
            val latestState = viewModel.state.value
            assertFalse(latestState.isLoading)
            assertEquals(latest, latestState.selectedDate)
            assertEquals(LocalDate.parse("2027-03-01"), latestState.loadedStart)

            oldGate.complete(Unit)
            advanceUntilIdle()

            assertEquals(latestState, viewModel.state.value)
            assertEquals(2, repository.eventLoadCount)
        }

    @Test
    fun returningToTheLoadedWindowCancelsAnUnneededNavigationLoadWithoutReloading() =
        runTest {
            val repository = FakeCalendarRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()
            val gate = CompletableDeferred<Unit>()
            repository.pendingLoad = gate
            viewModel.onEvent(CalendarCommand.SelectDate(LocalDate.parse("2027-04-15")))
            runCurrent()
            assertEquals(2, repository.eventLoadCount)

            viewModel.onEvent(CalendarCommand.SelectDate(LocalDate.parse("2026-10-18")))
            assertFalse(viewModel.state.value.isLoading)
            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(2, repository.eventLoadCount)
            assertEquals(LocalDate.parse("2026-08-31"), viewModel.state.value.loadedStart)
            assertEquals(LocalDate.parse("2026-10-18"), viewModel.state.value.selectedDate)
        }

    @Test
    fun explicitRefreshReplacesCachedDataAndIsNotCancelledBySelectingAnotherCoveredDate() =
        runTest {
            val repository = FakeCalendarRepository().apply { loadEvents = { listOf(draft.toEvent(1)) } }
            val viewModel = createViewModel(repository)
            advanceUntilIdle()
            val gate = CompletableDeferred<Unit>()
            repository.pendingLoad = gate
            repository.loadEvents = { listOf(draft.copy(title = "Обновлено").toEvent(1)) }

            viewModel.onEvent(CalendarCommand.Refresh)
            runCurrent()
            viewModel.onEvent(CalendarCommand.SelectDate(LocalDate.parse("2026-11-20")))
            assertTrue(viewModel.state.value.isLoading)
            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(2, repository.eventLoadCount)
            assertEquals("Обновлено", viewModel.state.value.events.single().title)
            assertEquals(LocalDate.parse("2026-11-20"), viewModel.state.value.selectedDate)
        }

    @Test
    fun mutationCompletionReloadsTheLatestSelectionEvenIfItsMonthWasAlreadyCached() =
        runTest {
            val repository = FakeCalendarRepository()
            val viewModel = createViewModel(repository)
            advanceUntilIdle()
            val gate = CompletableDeferred<Unit>()
            repository.pendingMutation = gate

            viewModel.onEvent(CalendarCommand.SaveEvent(draft, eventId = 42))
            runCurrent()
            viewModel.onEvent(CalendarCommand.SelectDate(LocalDate.parse("2026-11-20")))
            assertEquals(1, repository.eventLoadCount)
            gate.complete(Unit)
            advanceUntilIdle()

            assertEquals(2, repository.eventLoadCount)
            assertEquals(LocalDate.parse("2026-09-28"), viewModel.state.value.loadedStart)
            assertEquals(LocalDate.parse("2026-11-20"), viewModel.state.value.selectedDate)
            assertEquals(CalendarEffect.EventSaved(42), viewModel.state.value.result)
        }

    @Test
    fun movePeriodUsesDaysWeeksOrMonthsForTheSelectedMode() =
        runTest {
            val viewModel = createViewModel()
            advanceUntilIdle()

            listOf(
                Triple(CalendarMode.Day, 2L, LocalDate.parse("2026-10-19")),
                Triple(CalendarMode.Week, -2L, LocalDate.parse("2026-10-03")),
                Triple(CalendarMode.Month, 2L, LocalDate.parse("2026-12-17")),
            ).forEach { (mode, offset, expected) ->
                viewModel.onEvent(CalendarCommand.SelectDate(LocalDate.parse("2026-10-17")))
                viewModel.onEvent(CalendarCommand.SelectMode(mode))
                viewModel.onEvent(CalendarCommand.MovePeriod(offset))
                advanceUntilIdle()
                assertEquals(expected, viewModel.state.value.selectedDate)
            }

            viewModel.onEvent(CalendarCommand.SelectDate(LocalDate.parse("2028-01-31")))
            viewModel.onEvent(CalendarCommand.MovePeriod(1))
            advanceUntilIdle()
            assertEquals(LocalDate.parse("2028-02-29"), viewModel.state.value.selectedDate)
        }

    private fun createViewModel(
        repository: FakeCalendarRepository = FakeCalendarRepository(),
        savedState: SavedStateHandle = SavedStateHandle(),
    ): CalendarViewModel {
        val auth = FakeAuthRepository()
        return CalendarViewModel(repository, auth, CalendarLoader(repository, auth), clock, savedState)
    }

    private class FakeCalendarRepository : CalendarRepository {
        val savedEventIds = mutableListOf<Int?>()
        val deletedEventIds = mutableListOf<Int>()
        var eventLoadCount = 0
        var mutationError: Exception? = null
        var pendingLoad: CompletableDeferred<Unit>? = null
        var pendingMutation: CompletableDeferred<Unit>? = null
        var loadEvents: suspend (CalendarQuery) -> List<CalendarEvent> = { emptyList() }

        override suspend fun getUsers(): Result<List<CalendarUser>> = Result.success(listOf(DemoAccount.user))

        override suspend fun getEvents(query: CalendarQuery): Result<List<CalendarEvent>> {
            eventLoadCount++
            pendingLoad?.await()
            return Result.success(loadEvents(query))
        }

        override suspend fun getVisibleOwnerIds(): Result<Set<Int>> = Result.success(setOf(DemoAccount.user.id))

        override suspend fun getAccessibleOwnerIds(): Result<Set<Int>> = Result.success(setOf(DemoAccount.user.id))

        override suspend fun setVisibleOwnerIds(ownerIds: Set<Int>): Result<Unit> = Result.success(Unit)

        override suspend fun saveEvent(
            draft: EventDraft,
            eventId: Int?,
        ): Result<CalendarEvent> {
            savedEventIds += eventId
            pendingMutation?.await()
            mutationError?.let { return Result.failure(it) }
            return Result.success(draft.toEvent(eventId ?: 1))
        }

        override suspend fun deleteEvent(eventId: Int): Result<Unit> {
            deletedEventIds += eventId
            mutationError?.let { return Result.failure(it) }
            return Result.success(Unit)
        }
    }

    private class FakeAuthRepository : AuthRepository {
        override val currentUser = MutableStateFlow<CalendarUser?>(DemoAccount.user)

        override suspend fun login(
            email: String,
            password: String,
        ): Result<CalendarUser> = Result.success(DemoAccount.user)

        override suspend fun logout(): Result<Unit> {
            currentUser.value = null
            return Result.success(Unit)
        }
    }
}

private fun EventDraft.toEvent(id: Int) =
    CalendarEvent(
        id = id,
        ownerId = DemoAccount.user.id,
        ownerName = DemoAccount.user.fullName,
        title = title,
        description = description,
        location = location,
        start = start,
        end = end,
        allDay = allDay,
        status = status,
        displayColor = DemoAccount.user.color,
    )
