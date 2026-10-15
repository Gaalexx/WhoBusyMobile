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
) {
    init {
        require(title.isNotBlank())
        require(end > start)
    }
}
