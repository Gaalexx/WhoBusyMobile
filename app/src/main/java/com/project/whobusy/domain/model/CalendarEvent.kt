package com.project.whobusy.domain.model

import java.time.LocalDateTime

data class CalendarEvent(
    val id: Int,
    val ownerId: Int,
    val ownerName: String,
    val title: String,
    val description: String,
    val location: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val allDay: Boolean,
    val status: EventStatus,
    val displayColor: String,
    val recurrenceRule: String? = null,
    val spaceId: Int? = null,
    val shares: List<EventShare> = emptyList(),
    val busyOnly: Boolean = false,
    val canEdit: Boolean = false,
    val color: String? = null,
    // Local occurrence metadata; never sent to the backend when a series is edited.
    val seriesStart: LocalDateTime? = null,
    val seriesEnd: LocalDateTime? = null,
)
