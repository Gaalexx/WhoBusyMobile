package com.project.whobusy.presentation.event.preview

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.domain.model.Space
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.presentation.event.model.EventFormState
import com.project.whobusy.presentation.event.viewmodel.EditorState
import com.project.whobusy.time.LocalDateTime
import com.project.whobusy.time.of
import com.project.whobusy.time.toLocalDate

internal object EventPreviewData {
    val spaces =
        listOf(
            Space(1, "Momentum", SpaceRole.Admin, 8, "MOMENTUM2026"),
            Space(2, "Дизайн-команда", SpaceRole.Actor, 6),
            Space(3, "Близкие", SpaceRole.Visitor, 4),
        )
    val event =
        CalendarEvent(
            1,
            1,
            "Гайдуков Александр",
            "Работа над макетом",
            "Главный экран и навигация",
            "Удалённо",
            LocalDateTime.of(2026, 10, 17, 10, 0),
            LocalDateTime.of(2026, 10, 17, 11, 30),
            false,
            EventStatus.BUSY,
            "#2481CC",
            "FREQ=WEEKLY;BYDAY=MO,TU,WE,TH,FR",
            shares = listOf(EventShare(1, EventVisibility.Actors), EventShare(2, EventVisibility.Busy)),
            canEdit = true,
        )
    val editor =
        EditorState(
            form = EventFormState.initial(event.start.toLocalDate(), event),
            spaces = spaces,
            isLoading = false,
        )
}
