package com.project.whobusy.presentation.calendar.preview

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.atTime
import com.project.whobusy.time.of
import com.project.whobusy.time.plusDays

object CalendarPreviewData {
    val date: LocalDate = LocalDate.of(2026, 10, 15)
    val user = CalendarUser(1, "alex@example.com", "Александр Гайдуков", UserRole.USER, "#2481CC", true)
    val anna = CalendarUser(2, "anna@example.com", "Анна Смирнова", UserRole.USER, "#6D4F9C", true)
    val mikhail = CalendarUser(3, "mikhail@example.com", "Михаил Волков", UserRole.USER, "#357F59", true)
    val events =
        listOf(
            event(1, user, "Синхронизация команды", 9, 10),
            event(2, anna, "Работа над макетом", 10, 11).copy(end = date.atTime(11, 30)),
            event(3, mikhail, "Можно созвониться", 12, 13).copy(status = EventStatus.FREE),
            event(4, user, "Обсуждение релиза", 14, 15).copy(status = EventStatus.TENTATIVE),
            event(5, anna, "Отпуск", 0, 0).copy(allDay = true, end = date.plusDays(1).atStartOfDay()),
        )
    val state =
        CalendarState(
            date,
            user = user,
            users = listOf(user, anna, mikhail),
            events = events,
            visibleOwnerIds = setOf(2, 3),
            accessibleOwnerIds = setOf(2, 3),
            isLoading = false,
        )

    private fun event(
        id: Int,
        owner: CalendarUser,
        title: String,
        start: Int,
        end: Int,
    ) = CalendarEvent(
        id,
        owner.id,
        owner.fullName,
        title,
        "Обсудим прогресс, планы на неделю и вопросы по текущим задачам.",
        "Переговорная № 2",
        date.atTime(start, 0),
        date.atTime(end, 0),
        false,
        EventStatus.BUSY,
        owner.color,
    )
}
