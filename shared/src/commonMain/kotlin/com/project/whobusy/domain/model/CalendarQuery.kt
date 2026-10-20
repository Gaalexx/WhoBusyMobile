package com.project.whobusy.domain.model

import com.project.whobusy.time.LocalDateTime

data class CalendarQuery(
    val start: LocalDateTime,
    val end: LocalDateTime,
    val ownerIds: Set<Int> = emptySet(),
    val spaceIds: Set<Int>? = null,
    val showOwn: Boolean? = null,
) {
    init {
        require(end > start) { "Calendar range end must be after start" }
    }
}
