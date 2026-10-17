package com.project.whobusy.domain.model

import java.time.LocalDateTime

data class EventDraft(
    val title: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val description: String = "",
    val location: String = "",
    val allDay: Boolean = false,
    val status: EventStatus = EventStatus.BUSY,
    val spaceId: Int? = null,
    val shares: List<EventShare> = emptyList(),
    val recurrenceRule: String? = null,
    val color: String? = null,
) {
    init {
        require(title.isNotBlank())
        require(end > start)
    }
}
