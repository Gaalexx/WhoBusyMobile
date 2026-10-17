package com.project.whobusy.presentation.calendar.viewmodel

import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import com.project.whobusy.presentation.calendar.model.expandRecurringEvents
import com.project.whobusy.presentation.calendar.model.monthDates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

class CalendarLoader
@Inject
constructor(
    private val repository: CalendarRepository,
    private val auth: AuthRepository,
    private val workspace: WorkspaceRepository? = null,
) {
    suspend fun load(date: LocalDate): CalendarState {
        val user = auth.currentUser.first()
        val dates = monthDates(YearMonth.from(date))
        val spaces = workspace?.getSpaces()?.getOrThrow().orEmpty()
        val board = workspace?.getBoardSettings()?.getOrThrow() ?: BoardSettings(emptySet())
        val events =
            repository
                .getEvents(
                    CalendarQuery(dates.first().atStartOfDay(), dates.last().plusDays(1).atStartOfDay()),
                ).getOrThrow()
        val users =
            (
                listOfNotNull(user) +
                    events.map {
                        CalendarUser(it.ownerId, null, it.ownerName, UserRole.USER, it.displayColor, true)
                    }
                ).distinctBy { it.id }
        val expanded =
            when {
                events.isEmpty() -> emptyList()
                events.none { it.recurrenceRule != null } ->
                    expandRecurringEvents(
                        events,
                        dates.first().atStartOfDay(),
                        dates.last().plusDays(1).atStartOfDay(),
                    )

                else ->
                    withContext(Dispatchers.Default) {
                        expandRecurringEvents(
                            events,
                            dates.first().atStartOfDay(),
                            dates.last().plusDays(1).atStartOfDay(),
                            checkCancellation = { ensureActive() },
                        )
                    }
            }
        return CalendarState(
            date,
            user = user,
            users = users,
            visibleOwnerIds = events.map { it.ownerId }.toSet(),
            accessibleOwnerIds = events.map { it.ownerId }.toSet(),
            spaces = spaces,
            boardSettings = board,
            series = events,
            events = expanded,
            isLoading = false,
        )
    }
}
