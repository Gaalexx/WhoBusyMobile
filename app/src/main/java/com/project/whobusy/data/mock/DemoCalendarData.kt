package com.project.whobusy.data.mock

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.model.UserRole
import java.time.LocalDate

object DemoCalendarData {
    val users =
        listOf(
            DemoAccount.user,
            CalendarUser(2, "anna@example.test", "Анна Смирнова", UserRole.USER, "#6D4F9C", true),
            CalendarUser(3, "mikhail@example.test", "Михаил Волков", UserRole.USER, "#357F59", true),
            CalendarUser(4, "daria@example.test", "Дарья Орлова", UserRole.USER, "#9B7023", true),
        )

    fun events(today: LocalDate): List<CalendarEvent> =
        listOf(
            event(1, users[0], "Синхронизация команды", today, 9, 10, "Переговорная № 2"),
            event(2, users[1], "Работа над макетом", today, 10, 11, "Удалённо").copy(end = today.atTime(11, 30)),
            event(3, users[2], "Можно созвониться", today, 12, 13, "Онлайн").copy(status = EventStatus.FREE),
            event(4, users[0], "Встреча команды", today.plusDays(1), 10, 11, "Онлайн"),
            event(5, users[1], "Свободное время", today.plusDays(2), 12, 13, "").copy(
                status = EventStatus.FREE,
            ),
            event(6, users[0], "Предварительная встреча", today.minusDays(1), 15, 16, "").copy(
                status = EventStatus.TENTATIVE,
            ),
            event(7, users[2], "Командный день", today.plusDays(3), 0, 0, "Офис").copy(
                end = today.plusDays(4).atStartOfDay(),
                allDay = true,
            ),
            event(
                8,
                users[0],
                "Обсуждение релиза",
                today,
                14,
                15,
                "Переговорная № 2",
            ).copy(status = EventStatus.TENTATIVE),
            event(9, users[1], "Отпуск", today, 0, 0, "").copy(end = today.plusDays(1).atStartOfDay(), allDay = true),
            event(10, users[0], "Синхронизация команды", today.minusDays(3), 9, 10, "Онлайн"),
            event(11, users[1], "Работа над макетом", today.minusDays(2), 10, 12, "Удалённо"),
        )

    private fun event(
        id: Int,
        owner: CalendarUser,
        title: String,
        day: LocalDate,
        startHour: Int,
        endHour: Int,
        location: String,
    ) = CalendarEvent(
        id = id,
        ownerId = owner.id,
        ownerName = owner.fullName,
        title = title,
        description = "",
        location = location,
        start = day.atTime(startHour, 0),
        end = day.atTime(endHour, 0),
        allDay = false,
        status = EventStatus.BUSY,
        displayColor = owner.color,
    )
}
