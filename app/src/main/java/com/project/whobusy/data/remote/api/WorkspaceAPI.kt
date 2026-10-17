package com.project.whobusy.data.remote.api

import com.project.whobusy.data.network.AuthenticatedHttpClient
import com.project.whobusy.data.remote.dto.BoardSettingsDTO
import com.project.whobusy.data.remote.dto.InvitationRequestDTO
import com.project.whobusy.data.remote.dto.JoinSpaceRequestDTO
import com.project.whobusy.data.remote.dto.ManagedUserCreateRequestDTO
import com.project.whobusy.data.remote.dto.ManagedUserUpdateRequestDTO
import com.project.whobusy.data.remote.dto.MemberRoleRequestDTO
import com.project.whobusy.data.remote.dto.ProfileUpdateRequestDTO
import com.project.whobusy.data.remote.dto.SpaceDTO
import com.project.whobusy.data.remote.dto.SpaceInvitationDTO
import com.project.whobusy.data.remote.dto.SpaceMemberDTO
import com.project.whobusy.data.remote.dto.SpaceNameRequestDTO
import com.project.whobusy.data.remote.dto.SpaceRoleDTO
import com.project.whobusy.data.remote.dto.UserActivityRequestDTO
import com.project.whobusy.data.remote.dto.UserDTO
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.ManagedUserDraft
import com.project.whobusy.domain.model.ProfileDraft
import com.project.whobusy.domain.model.SpaceRole
import io.ktor.client.call.body
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import javax.inject.Inject

class WorkspaceAPI
@Inject
constructor(
    private val client: AuthenticatedHttpClient,
) {
    suspend fun getSpaces(): List<SpaceDTO> = client.request("spaces").body()

    suspend fun createSpace(name: String): SpaceDTO =
        client.request("spaces", HttpMethod.Post) {
            contentType(ContentType.Application.Json)
            setBody(SpaceNameRequestDTO(name.trim()))
        }.body()

    suspend fun renameSpace(spaceId: Int, name: String): SpaceDTO =
        client.request("spaces/$spaceId", HttpMethod.Patch) {
            contentType(ContentType.Application.Json)
            setBody(SpaceNameRequestDTO(name.trim()))
        }.body()

    suspend fun deleteSpace(spaceId: Int) {
        client.request("spaces/$spaceId", HttpMethod.Delete)
    }

    suspend fun joinSpace(code: String): SpaceDTO =
        client.request("spaces/join", HttpMethod.Post) {
            contentType(ContentType.Application.Json)
            setBody(JoinSpaceRequestDTO(code.trim()))
        }.body()

    suspend fun leaveSpace(spaceId: Int) {
        client.request("spaces/$spaceId/leave", HttpMethod.Post)
    }

    suspend fun regenerateInviteCode(spaceId: Int): SpaceDTO =
        client.request("spaces/$spaceId/invite-code", HttpMethod.Post).body()

    suspend fun getBoardSettings(): BoardSettingsDTO = client.request("users/me/board").body()

    suspend fun saveBoardSettings(settings: BoardSettings): BoardSettingsDTO =
        client.request("users/me/board", HttpMethod.Put) {
            contentType(ContentType.Application.Json)
            setBody(BoardSettingsDTO.fromDomain(settings))
        }.body()

    suspend fun getMembers(spaceId: Int): List<SpaceMemberDTO> = client.request("spaces/$spaceId/members").body()

    suspend fun updateMemberRole(spaceId: Int, userId: Int, role: SpaceRole): SpaceMemberDTO =
        client.request("spaces/$spaceId/members/$userId", HttpMethod.Patch) {
            contentType(ContentType.Application.Json)
            setBody(MemberRoleRequestDTO(SpaceRoleDTO.fromDomain(role)))
        }.body()

    suspend fun removeMember(spaceId: Int, userId: Int) {
        client.request("spaces/$spaceId/members/$userId", HttpMethod.Delete)
    }

    suspend fun getInvitations(): List<SpaceInvitationDTO> = client.request("invitations").body()

    suspend fun getSpaceInvitations(spaceId: Int): List<SpaceInvitationDTO> =
        client.request("spaces/$spaceId/invitations").body()

    suspend fun invite(spaceId: Int, login: String, role: SpaceRole): SpaceInvitationDTO =
        client.request("spaces/$spaceId/invitations", HttpMethod.Post) {
            contentType(ContentType.Application.Json)
            setBody(InvitationRequestDTO(login.trim().lowercase(), SpaceRoleDTO.fromDomain(role)))
        }.body()

    suspend fun cancelInvitation(spaceId: Int, invitationId: Int) {
        client.request("spaces/$spaceId/invitations/$invitationId", HttpMethod.Delete)
    }

    suspend fun acceptInvitation(invitationId: Int): SpaceDTO =
        client.request("invitations/$invitationId/accept", HttpMethod.Post).body()

    suspend fun declineInvitation(invitationId: Int) {
        client.request("invitations/$invitationId/decline", HttpMethod.Post)
    }

    suspend fun updateProfile(draft: ProfileDraft): UserDTO =
        client.request("auth/me", HttpMethod.Patch) {
            contentType(ContentType.Application.Json)
            setBody(ProfileUpdateRequestDTO.fromDomain(draft))
        }.body()

    suspend fun getUsers(): List<UserDTO> = client.request("users").body()

    suspend fun createUser(draft: ManagedUserDraft): UserDTO =
        client.request("users", HttpMethod.Post) {
            contentType(ContentType.Application.Json)
            setBody(ManagedUserCreateRequestDTO.fromDomain(draft))
        }.body()

    suspend fun updateUser(userId: Int, draft: ManagedUserDraft): UserDTO =
        client.request("users/$userId", HttpMethod.Patch) {
            contentType(ContentType.Application.Json)
            setBody(ManagedUserUpdateRequestDTO.fromDomain(draft))
        }.body()

    suspend fun deactivateUser(userId: Int): UserDTO =
        client.request("users/$userId", HttpMethod.Patch) {
            contentType(ContentType.Application.Json)
            setBody(UserActivityRequestDTO(isActive = false))
        }.body()

    suspend fun deleteUser(userId: Int) {
        client.request("users/$userId", HttpMethod.Delete)
    }
}
