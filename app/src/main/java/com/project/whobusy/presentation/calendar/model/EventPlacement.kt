package com.project.whobusy.presentation.calendar.model

import com.project.whobusy.domain.model.CalendarEvent
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class EventPlacement(
    val event: CalendarEvent,
    val startMinute: Int,
    val endMinute: Int,
    val lane: Int,
    val laneCount: Int,
)

fun placeEvents(
    events: List<CalendarEvent>,
    date: LocalDate,
): List<EventPlacement> {
    val ordered = eventsOnDate(events, date).filterNot { it.allDay }
    val result = mutableListOf<EventPlacement>()
    val group = mutableListOf<EventPlacement>()
    val laneEnds = mutableListOf<Int>()
    var groupEnd = 0

    fun flush() {
        result.addAll(group.map { it.copy(laneCount = laneEnds.size) })
        group.clear()
        laneEnds.clear()
    }
    ordered.forEach { event ->
        val start =
            ChronoUnit.MINUTES
                .between(date.atStartOfDay(), event.start)
                .toInt()
                .coerceIn(0, 1439)
        val end =
            ChronoUnit.MINUTES
                .between(date.atStartOfDay(), event.end)
                .toInt()
                .coerceIn(start + 1, 1440)
        if (start >= groupEnd) flush()
        var lane = laneEnds.indexOfFirst { it <= start }
        if (lane == -1) {
            lane = laneEnds.size
            laneEnds.add(end)
        } else {
            laneEnds[lane] = end
        }
        group.add(EventPlacement(event, start, end, lane, 1))
        groupEnd = if (group.size == 1) end else maxOf(groupEnd, end)
    }
    flush()
    return result
}
