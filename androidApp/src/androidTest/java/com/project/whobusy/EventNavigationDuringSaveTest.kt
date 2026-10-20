package com.project.whobusy

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarLoader
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import com.project.whobusy.presentation.event.ui.EventDetailsRoot
import com.project.whobusy.presentation.event.ui.EventEditorRoot
import com.project.whobusy.presentation.event.viewmodel.EditorCommand
import com.project.whobusy.presentation.event.viewmodel.EventEditorViewModel
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.plan_date_range
import com.project.whobusy.resources.plan_edit_action
import com.project.whobusy.resources.plan_visibility
import com.project.whobusy.resources.ui_action_back
import com.project.whobusy.time.Clock
import com.project.whobusy.time.Instant
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.ZoneOffset
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.now
import com.project.whobusy.time.parse
import com.project.whobusy.time.plusDays
import com.project.whobusy.time.value
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EventNavigationDuringSaveTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val viewModelStore = ViewModelStore()

    @After
    fun clearViewModels() {
        composeRule.runOnUiThread { viewModelStore.clear() }
    }

    @Test
    fun editorBlocksDateVisibilityAndBackUntilSaveCompletesOnce() {
        val repositories = TestRepositories()
        val repository = GatedCalendarRepository(repositories.calendar)
        val backCount = AtomicInteger()
        val datesCount = AtomicInteger()
        val visibilityCount = AtomicInteger()
        val savedCount = AtomicInteger()
        lateinit var viewModel: EventEditorViewModel
        composeRule.runOnUiThread {
            val factory = viewModelFactory {
                initializer {
                    EventEditorViewModel(
                        repository,
                        repositories.workspace,
                        repositories.auth,
                        repositories.clock,
                        SavedStateHandle(),
                    )
                }
            }
            viewModel = ViewModelProvider(viewModelStore, factory)[EventEditorViewModel::class]
            viewModel.start(repositories.date.toString(), null, null)
        }
        composeRule.setContent {
            WhoBusyTheme {
                EventEditorRoot(
                    viewModel = viewModel,
                    onBack = { backCount.incrementAndGet() },
                    onVisibility = { visibilityCount.incrementAndGet() },
                    onDates = { datesCount.incrementAndGet() },
                    onSaved = { savedCount.incrementAndGet() },
                )
            }
        }
        composeRule.waitUntil(5_000) { !viewModel.state.value.isLoading }
        composeRule.runOnUiThread {
            viewModel.onEvent(EditorCommand.Title("План с отложенным сохранением"))
            viewModel.onEvent(EditorCommand.Save)
        }
        composeRule.waitUntil(5_000) { viewModel.state.value.isSaving }

        composeRule.onNode(hasText(resourceText(Res.string.plan_date_range)) and hasClickAction())
            .performScrollTo()
            .performClick()
        composeRule.onNode(hasText(resourceText(Res.string.plan_visibility)) and hasClickAction())
            .performScrollTo()
            .performClick()
        composeRule.onNodeWithContentDescription(backLabel()).performClick()
        assertEquals(0, datesCount.get())
        assertEquals(0, visibilityCount.get())
        assertEquals(0, backCount.get())
        assertEquals(0, savedCount.get())

        repository.mutationGate.complete(Unit)
        composeRule.waitUntil(5_000) { savedCount.get() == 1 }
        composeRule.waitForIdle()
        assertEquals(1, savedCount.get())
        assertEquals(1, repository.saveCount.get())
        assertFalse(viewModel.state.value.isSaving)
        assertFalse(viewModel.state.value.isSaved)
    }

    @Test
    fun detailsBlocksTopBarEditAndBackUntilDeleteCompletesOnce() {
        val repositories = TestRepositories()
        val repository = GatedCalendarRepository(repositories.calendar)
        val backCount = AtomicInteger()
        val editCount = AtomicInteger()
        val deletedCount = AtomicInteger()
        val event = runBlocking {
            repositories.calendar.getEvents(
                CalendarQuery(repositories.date.atStartOfDay(), repositories.date.plusDays(1).atStartOfDay()),
            ).getOrThrow().first { it.canEdit }
        }
        lateinit var viewModel: CalendarViewModel
        composeRule.runOnUiThread {
            val factory = viewModelFactory {
                initializer {
                    CalendarViewModel(
                        repository,
                        repositories.auth,
                        CalendarLoader(repository, repositories.auth, repositories.workspace),
                        repositories.clock,
                        SavedStateHandle(),
                    )
                }
            }
            viewModel = ViewModelProvider(viewModelStore, factory)[CalendarViewModel::class]
        }
        composeRule.waitUntil(5_000) { !viewModel.state.value.isLoading }
        composeRule.runOnUiThread { viewModel.rememberEvent(event) }
        composeRule.setContent {
            WhoBusyTheme {
                EventDetailsRoot(
                    eventId = event.id,
                    occurrenceStart = event.start.toString(),
                    onBack = { backCount.incrementAndGet() },
                    onEdit = { editCount.incrementAndGet() },
                    onDeleted = { deletedCount.incrementAndGet() },
                    viewModel = viewModel,
                )
            }
        }
        composeRule.runOnUiThread { viewModel.onEvent(CalendarCommand.DeleteEvent(event.id)) }
        composeRule.waitUntil(5_000) { viewModel.state.value.isSaving }

        composeRule.onNodeWithContentDescription(resourceText(Res.string.plan_edit_action)).performClick()
        composeRule.onNodeWithContentDescription(backLabel()).performClick()
        assertEquals(0, editCount.get())
        assertEquals(0, backCount.get())
        assertEquals(0, deletedCount.get())

        repository.mutationGate.complete(Unit)
        composeRule.waitUntil(5_000) { deletedCount.get() == 1 }
        composeRule.waitForIdle()
        assertEquals(1, deletedCount.get())
        assertEquals(1, repository.deleteCount.get())
        assertFalse(viewModel.state.value.isSaving)
        assertNull(viewModel.state.value.result)
    }

    private fun backLabel(): String = resourceText(Res.string.ui_action_back)

    private class GatedCalendarRepository(
        private val base: CalendarRepository,
    ) : CalendarRepository by base {
        val mutationGate = CompletableDeferred<Unit>()
        val saveCount = AtomicInteger()
        val deleteCount = AtomicInteger()

        override suspend fun saveEvent(draft: EventDraft, eventId: Int?): Result<CalendarEvent> {
            saveCount.incrementAndGet()
            mutationGate.await()
            return base.saveEvent(draft, eventId)
        }

        override suspend fun deleteEvent(eventId: Int): Result<Unit> {
            deleteCount.incrementAndGet()
            mutationGate.await()
            return base.deleteEvent(eventId)
        }
    }

    private class TestRepositories {
        val clock: Clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)
        val date: LocalDate = LocalDate.now(clock)
        private val session = SessionStore()
        private val accounts = MockAccountStore()
        val auth = MockAuthRepository(session, accounts)
        val workspace = MockWorkspaceRepository(session, accounts, clock)
        val calendar = MockCalendarRepository(session, clock, workspace, accounts)

        init {
            runBlocking { auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow() }
        }
    }
}
