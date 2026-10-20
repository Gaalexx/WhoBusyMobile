package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.EventDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventUpdateRequestDTO(
    val title: String,
    val start: String,
    val end: String,
    val description: String,
    val location: String,
    @SerialName("all_day") val allDay: Boolean,
    val status: String,
    val rrule: String?,
    val color: String?,
    // Placement is immutable. A manager editing a space plan may not send shares,
    // even an empty list: the backend reserves that field for the owner.
    val shares: List<EventShareDTO>? = null,
) {
    companion object {
        fun fromDomain(draft: EventDraft) =
            EventUpdateRequestDTO(
                title = draft.title.trim(),
                start = draft.start.toString(),
                end = draft.end.toString(),
                description = draft.description,
                location = draft.location,
                allDay = draft.allDay,
                status = draft.status.name.lowercase(),
                rrule = draft.recurrenceRule,
                color = draft.color,
                shares = if (draft.spaceId == null) draft.shares.map(EventShareDTO::fromDomain) else null,
            )
    }
}
