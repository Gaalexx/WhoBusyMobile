package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.EventDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventRequestDTO(
    val title: String,
    val start: String,
    val end: String,
    val description: String,
    val location: String,
    @SerialName("all_day") val allDay: Boolean,
    val status: String,
) {
    companion object {
        fun fromDomain(draft: EventDraft) =
            EventRequestDTO(
                draft.title.trim(),
                draft.start.toString(),
                draft.end.toString(),
                draft.description,
                draft.location,
                draft.allDay,
                draft.status.name.lowercase(),
            )
    }
}
