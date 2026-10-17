package com.project.whobusy.presentation.spaces.ui

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.model.Space
import com.project.whobusy.domain.model.SpaceInvitation
import com.project.whobusy.domain.model.SpaceMember
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import java.time.LocalDateTime

internal object SpacesPreviewData {
    val spaces =
        listOf(
            Space(1, "Momentum", SpaceRole.Admin, 6, "q7Xv_3kM9pLa"),
            Space(2, "Дизайн-команда", SpaceRole.Actor, 4),
            Space(3, "Близкие", SpaceRole.Visitor, 8),
        )
    private val now = LocalDateTime.of(2026, 10, 17, 10, 0)
    val members =
        listOf(
            SpaceMember(1, "gaalex", "Гайдуков Александр", "#2481CC", SpaceRole.Admin, now),
            SpaceMember(2, "anna", "Смирнова Анна", "#6D4F9C", SpaceRole.Moderator, now),
            SpaceMember(3, "mikhail", "Волков Михаил", "#357F59", SpaceRole.Actor, now),
            SpaceMember(4, "darya", "Орлова Дарья", "#6D4F9C", SpaceRole.Actor, now),
            SpaceMember(5, "ivan", "Петров Иван", "#72808D", SpaceRole.Visitor, now),
            SpaceMember(6, "elena", "Кузнецова Елена", "#357F59", SpaceRole.Visitor, now),
        )
    val invitations =
        listOf(
            SpaceInvitation(
                1,
                2,
                "Дизайн-команда",
                1,
                "gaalex",
                "Гайдуков Александр",
                SpaceRole.Actor,
                "Смирнова Анна",
                now,
            ),
            SpaceInvitation(
                2,
                4,
                "Поездка в Питер",
                1,
                "gaalex",
                "Гайдуков Александр",
                SpaceRole.Visitor,
                "Волков Михаил",
                now,
            ),
        )
    private val event =
        CalendarEvent(
            1,
            2,
            "Смирнова Анна",
            "Ужин вместе",
            "",
            "",
            now.withHour(18),
            now.withHour(19).withMinute(30),
            false,
            EventStatus.BUSY,
            "#6D4F9C",
        )
    val state =
        SpacesState(
            spaces = spaces,
            invitations = invitations,
            members = members,
            spaceId = 1,
            currentUserId = 1,
            name = "Momentum",
            successorId = 2,
            events =
                listOf(
                    event.copy(spaceId = 3),
                    event.copy(
                        id = 2,
                        ownerId = 3,
                        ownerName = "Волков Михаил",
                        title = "Занято",
                        start = now.withHour(20),
                        end = now.withHour(21),
                        busyOnly = true,
                        displayColor = "#72808D",
                    ),
                ),
        )
}
