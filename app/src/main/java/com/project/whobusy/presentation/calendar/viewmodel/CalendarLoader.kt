package com.project.whobusy.presentation.calendar.viewmodel

import com.project.whobusy.domain.model.CalendarQuery
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.presentation.calendar.model.monthDates
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.flow.first

class CalendarLoader @Inject constructor(
    private val repository: CalendarRepository,
    private val auth: AuthRepository,
) {
    suspend fun load(date: LocalDate): CalendarState {
        val user = auth.currentUser.first()
        val dates = monthDates(YearMonth.from(date))
        val users = repository.getUsers().getOrThrow()
        val visible = repository.getVisibleOwnerIds().getOrThrow()
        val accessible = repository.getAccessibleOwnerIds().getOrThrow()
        val events = repository.getEvents(
            CalendarQuery(dates.first().atStartOfDay(), dates.last().plusDays(1).atStartOfDay()),
        ).getOrThrow()
        return CalendarState(date, user = user, users = users, visibleOwnerIds = visible,
            accessibleOwnerIds = accessible, events = events, isLoading = false)
    }
}
