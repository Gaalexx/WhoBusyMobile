package com.project.whobusy.presentation

import com.project.whobusy.R
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesEffect
import com.project.whobusy.presentation.spaces.viewmodel.SpacesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
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
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

@OptIn(ExperimentalCoroutinesApi::class)
class SpacePlansViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun saturdaySpacePlansExcludeTheWeekdaySeriesMasterAndUseTheInjectedToday() =
        runTest {
            val clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)
            val session = SessionStore()
            val accounts = MockAccountStore()
            val auth = MockAuthRepository(session, accounts)
            val workspace = MockWorkspaceRepository(session, accounts, clock)
            val calendar = MockCalendarRepository(session, clock, workspace, accounts)
            auth.login("demo", DemoAccount.PASSWORD).getOrThrow()
            val viewModel = SpacesViewModel(workspace, calendar, auth, clock)

            viewModel.onEvent(SpacesCommand.Load(1))
            val state = viewModel.state.first { !it.isLoading }

            assertEquals(LocalDate.of(2026, 10, 17), state.today)
            assertFalse(state.events.any { it.id == 11 })
            assertTrue(
                state.events.all {
                    it.start < state.today.plusDays(1).atStartOfDay() &&
                        it.end > state.today.atStartOfDay()
                },
            )
        }

    @Test
    fun completedCreationRemainsInStateAndBlocksAnotherCreationUntilHandled() = runTest {
        val fixture = signedInFixture()
        val viewModel = fixture.viewModel
        val initialCount = fixture.workspace.getSpaces().getOrThrow().size
        viewModel.onEvent(SpacesCommand.NameChanged("Первое пространство"))
        viewModel.onEvent(SpacesCommand.Create)
        advanceUntilIdle()

        val firstResult = viewModel.state.value.result as SpacesEffect.OpenSpace
        assertFalse(viewModel.state.value.isSaving)
        viewModel.onEvent(SpacesCommand.NameChanged("Второе пространство"))
        viewModel.onEvent(SpacesCommand.Create)
        viewModel.onEvent(SpacesCommand.Load())
        advanceUntilIdle()

        assertEquals(firstResult, viewModel.state.value.result)
        assertEquals("Первое пространство", viewModel.state.value.name)
        assertEquals(initialCount + 1, fixture.workspace.getSpaces().getOrThrow().size)

        viewModel.onEvent(SpacesCommand.ResultHandled)
        assertNull(viewModel.state.value.result)
        viewModel.onEvent(SpacesCommand.NameChanged("Второе пространство"))
        viewModel.onEvent(SpacesCommand.Create)
        advanceUntilIdle()

        val secondResult = viewModel.state.value.result as SpacesEffect.OpenSpace
        assertNotEquals(firstResult.spaceId, secondResult.spaceId)
        assertEquals(initialCount + 2, fixture.workspace.getSpaces().getOrThrow().size)
    }

    @Test
    fun completionCanBeHandledWhileSavingFlagIsStillSet() = runTest {
        val viewModel = signedInFixture().viewModel
        val savingWhenHandled = mutableListOf<Boolean>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect { state ->
                if (state.result != null) {
                    savingWhenHandled += state.isSaving
                    viewModel.onEvent(SpacesCommand.ResultHandled)
                }
            }
        }

        viewModel.onEvent(SpacesCommand.NameChanged("Один переход"))
        viewModel.onEvent(SpacesCommand.Create)
        advanceUntilIdle()

        assertEquals(listOf(true), savingWhenHandled)
        assertNull(viewModel.state.value.result)
        assertFalse(viewModel.state.value.isSaving)
    }

    @Test
    fun copyingInviteCodeOnlyUpdatesMessageWithoutNavigationResult() = runTest {
        val viewModel = signedInFixture().viewModel
        viewModel.onEvent(SpacesCommand.Load(1))
        viewModel.state.first { !it.isLoading }

        viewModel.onEvent(SpacesCommand.CopyCode)

        assertEquals(R.string.spaces_key_copied, viewModel.state.value.message)
        assertNull(viewModel.state.value.result)
        assertFalse(viewModel.state.value.isSaving)
    }

    private suspend fun signedInFixture(): Fixture {
        val clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)
        val session = SessionStore()
        val accounts = MockAccountStore()
        val auth = MockAuthRepository(session, accounts)
        val workspace = MockWorkspaceRepository(session, accounts, clock)
        val calendar = MockCalendarRepository(session, clock, workspace, accounts)
        auth.login("demo", DemoAccount.PASSWORD).getOrThrow()
        return Fixture(workspace, SpacesViewModel(workspace, calendar, auth, clock))
    }

    private data class Fixture(val workspace: MockWorkspaceRepository, val viewModel: SpacesViewModel)
}
