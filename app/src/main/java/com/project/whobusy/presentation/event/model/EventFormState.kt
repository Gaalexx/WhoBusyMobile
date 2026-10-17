package com.project.whobusy.presentation.event.model

import androidx.annotation.StringRes
import androidx.compose.runtime.saveable.listSaver
import com.project.whobusy.R
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventDraft
import com.project.whobusy.domain.model.EventStatus
import java.time.LocalDate
import java.time.LocalDateTime

data class EventFormState(
    val title: String,
    val start: String,
    val end: String,
    val location: String = "",
    val description: String = "",
    val allDay: Boolean = false,
    val status: EventStatus = EventStatus.BUSY,
    @param:StringRes val validationError: Int? = null,
) {
    fun toDraft() =
        EventDraft(title, LocalDateTime.parse(start), LocalDateTime.parse(end), description, location, allDay, status)

    @StringRes
    fun validate(): Int? =
        when {
            title.isBlank() -> R.string.error_event_title_required
            LocalDateTime.parse(end) <= LocalDateTime.parse(start) -> R.string.error_event_end_after_start
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
        )

        val Saver =
            listSaver<EventFormState, Any>(
                save = { listOf(it.title, it.start, it.end, it.location, it.description, it.allDay, it.status.name) },
                restore = {
                    EventFormState(
                        it[0] as String,
                        it[1] as String,
                        it[2] as String,
                        it[3] as String,
                        it[4] as String,
                        it[5] as Boolean,
                        EventStatus.valueOf(it[6] as String),
                    )
                },
            )
    }
}
