package com.project.whobusy.data.repository

import com.project.whobusy.data.remote.api.WorkspaceAPI
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.ManagedUserDraft
import com.project.whobusy.domain.model.ProfileDraft
import com.project.whobusy.domain.model.Space
import com.project.whobusy.domain.model.SpaceInvitation
import com.project.whobusy.domain.model.SpaceMember
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.domain.repository.WorkspaceRepository
import javax.inject.Inject

class RemoteWorkspaceRepository
@Inject
constructor(
    private val api: WorkspaceAPI,
    private val sessionStore: SessionStore,
) : WorkspaceRepository {
    override suspend fun getSpaces(): Result<List<Space>> = repositoryResult { api.getSpaces().map { it.toDomain() } }

    override suspend fun createSpace(name: String): Result<Space> =
        repositoryResult { api.createSpace(name).toDomain() }

    override suspend fun renameSpace(spaceId: Int, name: String): Result<Space> =
        repositoryResult { api.renameSpace(spaceId, name).toDomain() }

    override suspend fun deleteSpace(spaceId: Int): Result<Unit> = repositoryResult { api.deleteSpace(spaceId) }

    override suspend fun joinSpace(code: String): Result<Space> = repositoryResult { api.joinSpace(code).toDomain() }

    override suspend fun leaveSpace(spaceId: Int): Result<Unit> = repositoryResult { api.leaveSpace(spaceId) }

    override suspend fun regenerateInviteCode(spaceId: Int): Result<Space> =
        repositoryResult { api.regenerateInviteCode(spaceId).toDomain() }

    override suspend fun getBoardSettings(): Result<BoardSettings> =
        repositoryResult { api.getBoardSettings().toDomain() }

    override suspend fun saveBoardSettings(settings: BoardSettings): Result<BoardSettings> =
        repositoryResult { api.saveBoardSettings(settings).toDomain() }

    override suspend fun getMembers(spaceId: Int): Result<List<SpaceMember>> =
        repositoryResult { api.getMembers(spaceId).map { it.toDomain() } }

    override suspend fun updateMemberRole(spaceId: Int, userId: Int, role: SpaceRole): Result<SpaceMember> =
        repositoryResult { api.updateMemberRole(spaceId, userId, role).toDomain() }

    override suspend fun removeMember(spaceId: Int, userId: Int): Result<Unit> =
        repositoryResult { api.removeMember(spaceId, userId) }

    override suspend fun getInvitations(): Result<List<SpaceInvitation>> =
        repositoryResult { api.getInvitations().map { it.toDomain() } }

    override suspend fun getSpaceInvitations(spaceId: Int): Result<List<SpaceInvitation>> =
        repositoryResult { api.getSpaceInvitations(spaceId).map { it.toDomain() } }

    override suspend fun invite(spaceId: Int, login: String, role: SpaceRole): Result<SpaceInvitation> =
        repositoryResult { api.invite(spaceId, login, role).toDomain() }

    override suspend fun cancelInvitation(spaceId: Int, invitationId: Int): Result<Unit> =
        repositoryResult { api.cancelInvitation(spaceId, invitationId) }

    override suspend fun acceptInvitation(invitationId: Int): Result<Space> =
        repositoryResult { api.acceptInvitation(invitationId).toDomain() }

    override suspend fun declineInvitation(invitationId: Int): Result<Unit> =
        repositoryResult { api.declineInvitation(invitationId) }

    override suspend fun updateProfile(draft: ProfileDraft): Result<CalendarUser> =
        repositoryResult {
            api.updateProfile(draft).toDomain().also(sessionStore::updateUser)
        }

    override suspend fun getUsers(): Result<List<CalendarUser>> =
        repositoryResult { api.getUsers().map { it.toDomain() } }

    override suspend fun saveUser(userId: Int?, draft: ManagedUserDraft): Result<CalendarUser> =
        repositoryResult {
            val user = if (userId == null) {
                val created = api.createUser(draft)
                // UserCreate has no is_active field; do not repeat the POST if this
                // follow-up PATCH fails. The created account already exists.
                if (draft.isActive) created else api.deactivateUser(created.id)
            } else {
                api.updateUser(userId, draft)
            }.toDomain()
            if (sessionStore.getUser()?.id == user.id) sessionStore.updateUser(user)
            user
        }

    override suspend fun deleteUser(userId: Int): Result<Unit> = repositoryResult { api.deleteUser(userId) }
}
