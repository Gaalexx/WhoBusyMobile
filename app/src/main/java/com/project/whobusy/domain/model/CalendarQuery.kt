package com.project.whobusy.domain.model

import java.time.LocalDateTime

data class CalendarQuery(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val ownerIds: Set<Int> = emptySet(),
) {
    init {
        require(end > start) { "Calendar range end must be after start" }
    }
}
