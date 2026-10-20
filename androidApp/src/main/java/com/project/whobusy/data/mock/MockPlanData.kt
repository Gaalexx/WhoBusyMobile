package com.project.whobusy.data.mock

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.model.EventVisibility
import java.time.LocalDate

internal object MockPlanData {
    fun events(today: LocalDate): List<CalendarEvent> =
        DemoCalendarData.events(today).map { event ->
            when (event.id) {
                1 -> event.copy(title = "Обсуждение релиза", spaceId = 1)
                2 ->
                    event.copy(
                        start = today.atTime(10, 30),
                        description = "Главный экран и навигация",
                        shares = listOf(EventShare(1, EventVisibility.Actors), EventShare(2, EventVisibility.Busy)),
                    )

                3 ->
                    event.copy(
                        title = "Личная встреча",
                        description = "Недоступные другим подробности",
                        status = EventStatus.BUSY,
                        shares = listOf(EventShare(1, EventVisibility.Busy)),
                    )

                4, 6, 8, 10 -> event.copy(spaceId = 1)
                5 -> event.copy(shares = listOf(EventShare(1, EventVisibility.Actors)))
                9 ->
                    event.copy(
                        start = today.minusDays(1).atStartOfDay(),
                        end = today.plusDays(2).atStartOfDay(),
                        shares = listOf(EventShare(1, EventVisibility.Actors)),
                    )

                11 ->
                    event.copy(
                        shares = listOf(EventShare(1, EventVisibility.Actors)),
                        recurrenceRule = "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR",
                    )

                7 -> event.copy(spaceId = 2)
                else -> event
            }
        }
}
