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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
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
    fun navigationEmitsRouteArgumentsWithoutChangingCalendarState() =
        runTest {
            val viewModel = createViewModel()
            val effects = collectEffects(viewModel)
            advanceUntilIdle()
            viewModel.onEvent(CalendarCommand.SelectDate(draft.start.toLocalDate()))
            advanceUntilIdle()
            val state = viewModel.state.value
            val event = draft.toEvent(42)

            viewModel.onEvent(CalendarCommand.OpenEvent(event))
            viewModel.onEvent(CalendarCommand.CreateEvent)
            viewModel.onEvent(CalendarCommand.OpenCalendars)
            viewModel.onEvent(CalendarCommand.EditEvent(event.id))
            advanceUntilIdle()

            assertEquals(
                listOf(
                    CalendarEffect.OpenEvent(42),
                    CalendarEffect.CreateEvent("2026-10-20"),
                    CalendarEffect.OpenCalendars,
                    CalendarEffect.EditEvent(42),
                ),
                effects,
            )
            assertEquals(state, viewModel.state.value)
        }

    @Test
    fun saveUsesExplicitEventIdAndEmitsSuccessAfterMutation() =
        runTest {
            val repository = FakeCalendarRepository()
            val viewModel = createViewModel(repository)
            val effects = collectEffects(viewModel)
            advanceUntilIdle()

            viewModel.onEvent(CalendarCommand.SaveEvent(draft))
            assertTrue(viewModel.state.value.isSaving)
            assertTrue(effects.isEmpty())
            advanceUntilIdle()
            viewModel.onEvent(CalendarCommand.SaveEvent(draft, eventId = 42))
            advanceUntilIdle()

            assertEquals(listOf(null, 42), repository.savedEventIds)
            assertEquals(listOf(CalendarEffect.EventSaved, CalendarEffect.EventSaved), effects)
            assertEquals(3, repository.eventLoadCount)
            assertFalse(viewModel.state.value.isSaving)
            assertNull(viewModel.state.value.error)
        }

    @Test
    fun deleteUsesExplicitEventIdAndRefreshesAfterSuccess() =
        runTest {
            val repository = FakeCalendarRepository()
            val viewModel = createViewModel(repository)
            val effects = collectEffects(viewModel)
            advanceUntilIdle()

            viewModel.onEvent(CalendarCommand.DeleteEvent(42))
            advanceUntilIdle()

            assertEquals(listOf(42), repository.deletedEventIds)
            assertEquals(listOf(CalendarEffect.EventDeleted), effects)
            assertEquals(2, repository.eventLoadCount)
            assertFalse(viewModel.state.value.isSaving)
        }

    @Test
    fun failedMutationsKeepErrorAndDoNotEmitSuccess() =
        runTest {
            val repository = FakeCalendarRepository().apply { mutationError = IllegalStateException("Unavailable") }
            val viewModel = createViewModel(repository)
            val effects = collectEffects(viewModel)
            advanceUntilIdle()

            viewModel.onEvent(CalendarCommand.SaveEvent(draft, eventId = 42))
            advanceUntilIdle()
            assertNotNull(viewModel.state.value.error)
            assertFalse(viewModel.state.value.isSaving)
            viewModel.onEvent(CalendarCommand.ClearError)
            assertNull(viewModel.state.value.error)
            viewModel.onEvent(CalendarCommand.DeleteEvent(42))
            advanceUntilIdle()

            assertNotNull(viewModel.state.value.error)
            assertFalse(viewModel.state.value.isSaving)
            assertFalse(viewModel.state.value.isLoading)
            assertEquals(1, repository.eventLoadCount)
            assertTrue(effects.isEmpty())
        }

    @Test
    fun restoresAndSavesDateAndModeWithoutStoringNavigation() =
        runTest {
            val savedState = SavedStateHandle(mapOf("date" to "2026-10-20", "mode" to CalendarMode.Week.name))
            val viewModel = createViewModel(savedState = savedState)
            val effects = collectEffects(viewModel)
            advanceUntilIdle()
            assertEquals(LocalDate.of(2026, 10, 20), viewModel.state.value.selectedDate)
            assertEquals(CalendarMode.Week, viewModel.state.value.mode)

            val selectedDate = LocalDate.of(2026, 11, 4)
            viewModel.onEvent(CalendarCommand.SelectDate(selectedDate))
            viewModel.onEvent(CalendarCommand.SelectMode(CalendarMode.Month))
            viewModel.onEvent(CalendarCommand.OpenCalendars)
            advanceUntilIdle()

            assertEquals(selectedDate, viewModel.state.value.selectedDate)
            assertEquals(CalendarMode.Month, viewModel.state.value.mode)
            assertEquals("2026-11-04", savedState.get<String>("date"))
            assertEquals(CalendarMode.Month.name, savedState.get<String>("mode"))
            assertEquals(setOf("date", "mode"), savedState.keys())
            assertEquals(listOf(CalendarEffect.OpenCalendars), effects)

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

    private fun TestScope.collectEffects(viewModel: CalendarViewModel): List<CalendarEffect> {
        val effects = mutableListOf<CalendarEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.effects.toList(effects)
        }
        return effects
    }

    private class FakeCalendarRepository : CalendarRepository {
        val savedEventIds = mutableListOf<Int?>()
        val deletedEventIds = mutableListOf<Int>()
        var eventLoadCount = 0
        var mutationError: Exception? = null

        override suspend fun getUsers(): Result<List<CalendarUser>> = Result.success(listOf(DemoAccount.user))

        override suspend fun getEvents(query: CalendarQuery): Result<List<CalendarEvent>> {
            eventLoadCount++
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
