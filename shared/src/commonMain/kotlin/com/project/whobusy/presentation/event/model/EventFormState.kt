package com.project.whobusy.presentation.event.model

import androidx.compose.runtime.saveable.listSaver
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.error_event_end_after_start
import com.project.whobusy.resources.error_event_title_required
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.atTime
import com.project.whobusy.time.parse
import org.jetbrains.compose.resources.StringResource

data class EventFormState(
    val title: String,
    val start: String,
    val end: String,
    val location: String = "",
    val description: String = "",
    val allDay: Boolean = false,
    val status: EventStatus = EventStatus.BUSY,
    val validationError: StringResource? = null,
    val spaceId: Int? = null,
    val shares: List<EventShare> = emptyList(),
    val recurrenceRule: String? = null,
    val color: String? = null,
) {
    fun toDraft() =
        EventDraft(
            title.trim(),
            LocalDateTime.parse(start),
            LocalDateTime.parse(end),
            description,
            location,
            allDay,
            status,
            spaceId,
            shares,
            recurrenceRule,
            color,
        )

    fun validate(): StringResource? =
        when {
            title.isBlank() -> Res.string.error_event_title_required
            LocalDateTime.parse(end) <= LocalDateTime.parse(start) -> Res.string.error_event_end_after_start
            else -> null
        }

    companion object {
        fun initial(
            date: LocalDate,
            event: CalendarEvent?,
        ) = EventFormState(
            event?.title.orEmpty(),
            (event?.start ?: date.atTime(14, 0)).toString(),
            (event?.end ?: date.atTime(15, 0)).toString(),
            event?.location.orEmpty(),
            event?.description.orEmpty(),
            event?.allDay ?: false,
            event?.status ?: EventStatus.BUSY,
            spaceId = event?.spaceId,
            shares = event?.shares.orEmpty(),
            recurrenceRule = event?.recurrenceRule,
            color = event?.color,
        )

        val Saver = listSaver<EventFormState, String>(save = { it.snapshot() }, restore = { restore(it) })

        fun restore(values: List<String>): EventFormState =
            EventFormState(
                title = values[0],
                start = values[1],
                end = values[2],
                location = values[3],
                description = values[4],
                allDay = values[5].toBoolean(),
                status = EventStatus.valueOf(values[6]),
                spaceId = values[7].toIntOrNull(),
                shares =
                    values[8].split(';').filter { it.isNotBlank() }.map { raw ->
                        val parts = raw.split(':')
                        EventShare(parts[0].toInt(), EventVisibility.valueOf(parts[1]))
                    },
                recurrenceRule = values[9].ifEmpty { null },
                color = values[10].ifEmpty { null },
            )
    }

    fun snapshot(): List<String> =
        listOf(
            title,
            start,
            end,
            location,
            description,
            allDay.toString(),
            status.name,
            spaceId?.toString().orEmpty(),
            shares.joinToString(";") { "${it.spaceId}:${it.visibility.name}" },
            recurrenceRule.orEmpty(),
            color.orEmpty(),
        )
}
