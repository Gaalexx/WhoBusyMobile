package com.project.whobusy.presentation

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
}
