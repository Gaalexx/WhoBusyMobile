package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventStatus
import java.time.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CalendarEventDTO(
    val id: Int,
    @SerialName("owner_id") val ownerId: Int,
    @SerialName("owner_name") val ownerName: String,
    val title: String,
    val description: String = "",
    val location: String = "",
    val start: String,
    val end: String,
    @SerialName("all_day") val allDay: Boolean = false,
    val status: String = "busy",
    @SerialName("display_color") val displayColor: String,
    val rrule: String? = null,
) {
    fun toDomain() = CalendarEvent(
        id = id,
        ownerId = ownerId,
        ownerName = ownerName,
        title = title,
        description = description,
        location = location,
        start = LocalDateTime.parse(start),
        end = LocalDateTime.parse(end),
        allDay = allDay,
        status = when (status) {
            "busy" -> EventStatus.BUSY
            "free" -> EventStatus.FREE
            "tentative" -> EventStatus.TENTATIVE
            else -> error("Unsupported event status: $status")
        },
        displayColor = displayColor,
        recurrenceRule = rrule,
    )
}
