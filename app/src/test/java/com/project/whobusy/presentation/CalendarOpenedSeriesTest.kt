package com.project.whobusy.presentation

import androidx.lifecycle.SavedStateHandle
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.presentation.calendar.model.expandRecurringEvents
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarLoader
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import com.project.whobusy.presentation.event.model.EventFormState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class CalendarOpenedSeriesTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun editingOccurrenceFromSpaceOutsideBoardKeepsTheOriginalSeriesStartAndEnd() =
        runTest {
            val clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)
            val session = SessionStore()
            val accounts = MockAccountStore()
            val auth = MockAuthRepository(session, accounts)
            val workspace = MockWorkspaceRepository(session, accounts, clock)
            val calendar = MockCalendarRepository(session, clock, workspace, accounts)
            auth.login("demo", DemoAccount.PASSWORD).getOrThrow()
            workspace.saveBoardSettings(BoardSettings(emptySet(), showOwn = false)).getOrThrow()
            val original =
                calendar
                    .saveEvent(
                        EventDraft(
                            title = "Давняя серия",
                            start = LocalDateTime.parse("1900-01-01T09:00"),
                            end = LocalDateTime.parse("1900-01-01T10:00"),
                            spaceId = 2,
                            recurrenceRule = "FREQ=DAILY",
                        ),
                    ).getOrThrow()
            val viewModel =
                CalendarViewModel(calendar, auth, CalendarLoader(calendar, auth, workspace), clock, SavedStateHandle())
            viewModel.state.first { !it.isLoading }
            assertTrue(
                viewModel.state.value.series
                    .none { it.id == original.id },
            )
            val occurrence =
                expandRecurringEvents(
                    listOf(original),
                    LocalDateTime.parse("2026-10-17T00:00"),
                    LocalDateTime.parse("2026-10-18T00:00"),
                ).single()

            viewModel.onEvent(CalendarCommand.OpenEvent(occurrence))
            val draft = EventFormState.initial(LocalDate.of(2026, 10, 17), viewModel.series(original.id)).toDraft()

            assertEquals(occurrence.start, viewModel.event(original.id)?.start)
            assertEquals(original.start, draft.start)
            assertEquals(original.end, draft.end)
            assertEquals(original.recurrenceRule, draft.recurrenceRule)
            val saved = calendar.saveEvent(draft.copy(title = "Новое название"), original.id).getOrThrow()
            assertEquals(original.start, saved.start)
            assertEquals(original.end, saved.end)
        }
}
