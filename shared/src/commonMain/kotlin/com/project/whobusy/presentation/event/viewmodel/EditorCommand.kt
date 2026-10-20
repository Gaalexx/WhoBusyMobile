package com.project.whobusy.presentation.event.viewmodel

import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.value

sealed interface EditorCommand {
    data class Title(
        val value: String,
    ) : EditorCommand

    data class Location(
        val value: String,
    ) : EditorCommand

    data class Description(
        val value: String,
    ) : EditorCommand

    data class Placement(
        val spaceId: Int?,
    ) : EditorCommand

    data class AllDay(
        val value: Boolean,
    ) : EditorCommand

    data class Start(
        val value: LocalDateTime,
    ) : EditorCommand

    data class End(
        val value: LocalDateTime,
    ) : EditorCommand

    data class Status(
        val value: EventStatus,
    ) : EditorCommand

    data class Recurrence(
        val value: String?,
    ) : EditorCommand

    data class Visibility(
        val spaceId: Int,
        val value: EventVisibility?,
    ) : EditorCommand

    data object Save : EditorCommand

    data object ResultHandled : EditorCommand
}
