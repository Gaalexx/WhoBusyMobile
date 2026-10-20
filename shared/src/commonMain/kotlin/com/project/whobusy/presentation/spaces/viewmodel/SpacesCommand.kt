package com.project.whobusy.presentation.spaces.viewmodel

import com.project.whobusy.domain.model.SpaceRole

sealed interface SpacesCommand {
    data class Load(
        val spaceId: Int? = null,
        val memberId: Int? = null,
    ) : SpacesCommand

    data class NameChanged(
        val value: String,
    ) : SpacesCommand

    data class CodeChanged(
        val value: String,
    ) : SpacesCommand

    data class LoginChanged(
        val value: String,
    ) : SpacesCommand

    data class RoleChanged(
        val role: SpaceRole,
    ) : SpacesCommand

    data class SuccessorSelected(
        val userId: Int,
    ) : SpacesCommand

    data class AcceptInvitation(
        val invitationId: Int,
    ) : SpacesCommand

    data class DeclineInvitation(
        val invitationId: Int,
    ) : SpacesCommand

    data class CancelInvitation(
        val invitationId: Int,
    ) : SpacesCommand

    data object Create : SpacesCommand

    data object Join : SpacesCommand

    data object Rename : SpacesCommand

    data object Delete : SpacesCommand

    data object Leave : SpacesCommand

    data object TransferAdmin : SpacesCommand

    data object SaveRole : SpacesCommand

    data object RemoveMember : SpacesCommand

    data object Invite : SpacesCommand

    data object RegenerateCode : SpacesCommand

    data object ShowPlans : SpacesCommand

    data object CopyCode : SpacesCommand

    data object ResultHandled : SpacesCommand
}
