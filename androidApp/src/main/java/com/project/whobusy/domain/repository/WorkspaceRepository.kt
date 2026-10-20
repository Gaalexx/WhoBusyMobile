package com.project.whobusy.domain.repository

import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.ManagedUserDraft
import com.project.whobusy.domain.model.ProfileDraft
import com.project.whobusy.domain.model.Space
import com.project.whobusy.domain.model.SpaceInvitation
import com.project.whobusy.domain.model.SpaceMember
import com.project.whobusy.domain.model.SpaceRole

interface WorkspaceRepository {
    suspend fun getSpaces(): Result<List<Space>>

    suspend fun createSpace(name: String): Result<Space>

    suspend fun renameSpace(
        spaceId: Int,
        name: String,
    ): Result<Space>

    suspend fun deleteSpace(spaceId: Int): Result<Unit>

    suspend fun joinSpace(code: String): Result<Space>

    suspend fun leaveSpace(spaceId: Int): Result<Unit>

    suspend fun regenerateInviteCode(spaceId: Int): Result<Space>

    suspend fun getBoardSettings(): Result<BoardSettings>

    suspend fun saveBoardSettings(settings: BoardSettings): Result<BoardSettings>

    suspend fun getMembers(spaceId: Int): Result<List<SpaceMember>>

    suspend fun updateMemberRole(
        spaceId: Int,
        userId: Int,
        role: SpaceRole,
    ): Result<SpaceMember>

    suspend fun removeMember(
        spaceId: Int,
        userId: Int,
    ): Result<Unit>

    suspend fun getInvitations(): Result<List<SpaceInvitation>>

    suspend fun getSpaceInvitations(spaceId: Int): Result<List<SpaceInvitation>>

    suspend fun invite(
        spaceId: Int,
        login: String,
        role: SpaceRole,
    ): Result<SpaceInvitation>

    suspend fun cancelInvitation(
        spaceId: Int,
        invitationId: Int,
    ): Result<Unit>

    suspend fun acceptInvitation(invitationId: Int): Result<Space>

    suspend fun declineInvitation(invitationId: Int): Result<Unit>

    suspend fun updateProfile(draft: ProfileDraft): Result<CalendarUser>

    suspend fun getUsers(): Result<List<CalendarUser>>

    suspend fun saveUser(
        userId: Int?,
        draft: ManagedUserDraft,
    ): Result<CalendarUser>

    suspend fun deleteUser(userId: Int): Result<Unit>
}
