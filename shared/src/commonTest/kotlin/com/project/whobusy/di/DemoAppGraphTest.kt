package com.project.whobusy.di

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.model.ProfileDraft
import com.project.whobusy.presentation.AppStartup
import com.project.whobusy.presentation.AppViewModel
import com.project.whobusy.time.LocalDateTime
import dev.zacsweers.metro.createGraph
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DemoAppGraphTest {
    @Test
    fun demoLoginSharesSessionAndStoresWithMetroViewModel() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val store = ViewModelStore()
        try {
            val graph = createGraph<DemoAppGraph>()
            val viewModel = ViewModelProvider.create(store, graph.metroViewModelFactory)[AppViewModel::class]
            runCurrent()

            assertEquals(AppStartup.Ready, viewModel.state.value.startup)
            assertNull(viewModel.state.value.user)

            val user = graph.auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow()
            runCurrent()

            assertEquals(user, graph.auth.currentUser.first())
            assertEquals(user, viewModel.state.value.user)
            assertTrue(graph.workspaces.getSpaces().getOrThrow().isNotEmpty())

            val space = graph.workspaces.createSpace("Общий граф").getOrThrow()
            val query = CalendarQuery(
                LocalDateTime.parse("2026-10-17T00:00"),
                LocalDateTime.parse("2026-10-18T00:00"),
                spaceIds = setOf(space.id),
            )
            val event = graph.calendar.saveEvent(
                EventDraft(
                    title = "Общий план",
                    start = LocalDateTime.parse("2026-10-17T10:00"),
                    end = LocalDateTime.parse("2026-10-17T11:00"),
                    spaceId = space.id,
                ),
            ).getOrThrow()

            assertEquals(event.id, graph.calendar.getEvents(query).getOrThrow().single { it.id == event.id }.id)

            val updated = graph.workspaces.updateProfile(
                ProfileDraft("Общий", "Пользователь", user.email, user.color),
            ).getOrThrow()
            runCurrent()

            assertEquals(updated, graph.auth.currentUser.first())
            assertEquals(updated, viewModel.state.value.user)
            assertEquals(updated, graph.calendar.getUsers().getOrThrow().single { it.id == user.id })

            graph.auth.logout().getOrThrow()
            runCurrent()

            assertNull(graph.auth.currentUser.first())
            assertNull(viewModel.state.value.user)
            assertTrue(graph.workspaces.getSpaces().exceptionOrNull() is AuthenticationException)
            assertTrue(graph.calendar.getEvents(query).exceptionOrNull() is AuthenticationException)
        } finally {
            store.clear()
            Dispatchers.resetMain()
        }
    }

    @Test
    fun graphInstancesKeepSessionsSeparate() = runTest {
        val first = createGraph<DemoAppGraph>()
        val second = createGraph<DemoAppGraph>()

        first.auth.login(DemoAccount.EMAIL, DemoAccount.PASSWORD).getOrThrow()

        assertEquals(DemoAccount.user.id, first.auth.currentUser.first()?.id)
        assertNull(second.auth.currentUser.first())
        assertTrue(second.workspaces.getSpaces().exceptionOrNull() is AuthenticationException)
    }
}

