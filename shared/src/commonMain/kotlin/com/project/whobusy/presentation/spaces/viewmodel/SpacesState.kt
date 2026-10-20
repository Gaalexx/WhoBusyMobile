package com.project.whobusy.presentation.spaces.viewmodel

import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.Space
import com.project.whobusy.domain.model.SpaceInvitation
import com.project.whobusy.domain.model.SpaceMember
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.of
import org.jetbrains.compose.resources.StringResource

data class SpacesState(
    val spaces: List<Space> = emptyList(),
    val invitations: List<SpaceInvitation> = emptyList(),
    val pendingInvitations: List<SpaceInvitation> = emptyList(),
    val members: List<SpaceMember> = emptyList(),
    val events: List<CalendarEvent> = emptyList(),
    val spaceId: Int? = null,
    val memberId: Int? = null,
    val currentUserId: Int? = null,
    val name: String = "",
    val code: String = "",
    val login: String = "",
    val selectedRole: SpaceRole = SpaceRole.Actor,
    val successorId: Int? = null,
    val showPlans: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val result: SpacesEffect? = null,
    val error: StringResource? = null,
    val message: StringResource? = null,
    val today: LocalDate = LocalDate.of(2026, 10, 17),
) {
    val space: Space?
        get() = spaces.firstOrNull { it.id == spaceId }

    val member: SpaceMember?
        get() = members.firstOrNull { it.userId == memberId }

    val isLastAdmin: Boolean
        get() = space?.myRole == SpaceRole.Admin && members.count { it.role == SpaceRole.Admin } == 1

    val successors: List<SpaceMember>
        get() = members.filter { it.userId != currentUserId }
}
