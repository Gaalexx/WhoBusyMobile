package com.project.whobusy.presentation

import androidx.lifecycle.SavedStateHandle
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.presentation.event.viewmodel.EditorCommand
import com.project.whobusy.presentation.event.viewmodel.EditorEffect
import com.project.whobusy.presentation.event.viewmodel.EventEditorViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
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
class EventEditorViewModelTest {
    private val clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)
    private val session = SessionStore()
    private val accounts = MockAccountStore()
    private val auth = MockAuthRepository(session, accounts)
    private val workspace = MockWorkspaceRepository(session, accounts, clock)
    private val calendar = MockCalendarRepository(session, clock, workspace, accounts)
    private val date = LocalDate.of(2026, 10, 17)

    @Before
    fun before() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun after() {
        Dispatchers.resetMain()
    }

    @Test
    fun newPersonalPlanDefaultsToBusyAndSpacePlanDropsShares() =
        runTest {
            auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow()
            val editor = editor()
            editor.start(date.toString(), null, null)
            advanceUntilIdle()
            assertEquals(
                setOf(1, 2, 3),
                editor.state.value.form.shares
                    .map { it.spaceId }
                    .toSet(),
            )
            assertTrue(
                editor.state.value.form.shares
                    .all { it.visibility == EventVisibility.Busy },
            )
            editor.onEvent(EditorCommand.Placement(1))
            assertEquals(1, editor.state.value.form.spaceId)
            assertTrue(
                editor.state.value.form.shares
                    .isEmpty(),
            )
        }

    @Test
    fun draftSurvivesRecreationAndRepeatedStartButResetDiscardsIt() =
        runTest {
            auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow()
            val saved = SavedStateHandle()
            val editor = editor(saved)
            editor.start(date.toString(), null, null)
            advanceUntilIdle()
            editor.onEvent(EditorCommand.Title("Черновик"))
            editor.onEvent(EditorCommand.Visibility(1, EventVisibility.Actors))
            editor.onEvent(EditorCommand.Visibility(2, null))
            editor.onEvent(EditorCommand.Recurrence("FREQ=MONTHLY"))
            editor.start(date.toString(), null, null)
            advanceUntilIdle()
            assertEquals("Черновик", editor.state.value.form.title)
            val restored = editor(saved)
            restored.start(date.toString(), null, null)
            advanceUntilIdle()
            assertEquals(editor.state.value.form, restored.state.value.form)
            restored.reset()
            restored.start(date.toString(), null, null)
            advanceUntilIdle()
            assertEquals("", restored.state.value.form.title)
            assertNull(restored.state.value.form.recurrenceRule)
        }

    @Test
    fun saveCreatesOnePlanWithChosenVisibilityAndEmitsAfterSuccess() =
        runTest {
            auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow()
            val editor = editor()
            val effects = mutableListOf<EditorEffect>()
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { editor.effects.toList(effects) }
            editor.start(date.toString(), null, null)
            advanceUntilIdle()
            editor.onEvent(EditorCommand.Title("План для акторов"))
            editor.onEvent(EditorCommand.Visibility(1, EventVisibility.Actors))
            editor.onEvent(EditorCommand.Visibility(2, null))
            editor.onEvent(EditorCommand.Visibility(3, null))
            editor.onEvent(EditorCommand.Save)
            editor.onEvent(EditorCommand.Save)
            advanceUntilIdle()
            val saved = calendar.getEvents(query()).getOrThrow().filter { it.title == "План для акторов" }
            assertEquals(1, saved.size)
            assertEquals(listOf(EventShare(1, EventVisibility.Actors)), saved.single().shares)
            assertEquals(listOf(EditorEffect.Saved), effects)
            assertFalse(editor.state.value.isSaving)
        }

    @Test
    fun managerCannotChangePlacementOrPersonalVisibilityOfExistingPlan() =
        runTest {
            auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow()
            val event = calendar.getEvents(query()).getOrThrow().first { it.id == 1 }
            val editor = editor()
            editor.start(date.toString(), event, null)
            advanceUntilIdle()
            editor.onEvent(EditorCommand.Placement(null))
            editor.onEvent(EditorCommand.Visibility(2, EventVisibility.Busy))
            assertEquals(1, editor.state.value.form.spaceId)
            assertTrue(
                editor.state.value.form.shares
                    .isEmpty(),
            )
        }

    @Test
    fun invalidDraftStaysOpenAndCanBeCorrected() =
        runTest {
            auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow()
            val editor = editor()
            editor.start(date.toString(), null, null)
            advanceUntilIdle()
            editor.onEvent(EditorCommand.Save)
            assertNotNull(editor.state.value.form.validationError)
            assertFalse(editor.state.value.isSaving)
            editor.onEvent(EditorCommand.Title("Исправленный план"))
            assertNull(editor.state.value.form.validationError)
        }

    private fun editor(saved: SavedStateHandle = SavedStateHandle()) =
        EventEditorViewModel(calendar, workspace, auth, clock, saved)

    private fun query() = CalendarQuery(date.atStartOfDay(), date.plusDays(1).atStartOfDay())
}
