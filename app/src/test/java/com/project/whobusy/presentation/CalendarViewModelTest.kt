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
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarEffect
import com.project.whobusy.presentation.calendar.viewmodel.CalendarLoader
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

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

        override suspend fun getUsers(): Result<List<CalendarUser>> = Result.success(listOf(DemoAccount.user))

        override suspend fun getEvents(query: CalendarQuery): Result<List<CalendarEvent>> {
            eventLoadCount++
            pendingLoad?.await()
            return Result.success(emptyList())
        }

        override suspend fun getVisibleOwnerIds(): Result<Set<Int>> = Result.success(setOf(DemoAccount.user.id))

        override suspend fun getAccessibleOwnerIds(): Result<Set<Int>> = Result.success(setOf(DemoAccount.user.id))

        override suspend fun setVisibleOwnerIds(ownerIds: Set<Int>): Result<Unit> = Result.success(Unit)

        override suspend fun saveEvent(
            draft: EventDraft,
            eventId: Int?,
        ): Result<CalendarEvent> {
            savedEventIds += eventId
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

        override fun logout() {
            currentUser.value = null
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
