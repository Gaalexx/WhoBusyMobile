package com.project.whobusy.data.mock

import com.project.whobusy.data.mock.MockWorkspaceData.InvitationRecord
import com.project.whobusy.data.mock.MockWorkspaceData.Membership
import com.project.whobusy.data.mock.MockWorkspaceData.SpaceRecord
import com.project.whobusy.data.repository.repositoryResult
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.ManagedUserDraft
import com.project.whobusy.domain.model.ProfileDraft
import com.project.whobusy.domain.model.Space
import com.project.whobusy.domain.model.SpaceInvitation
import com.project.whobusy.domain.model.SpaceMember
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.domain.repository.WorkspaceRepository
import java.time.Clock
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockWorkspaceRepository
@Inject
constructor(
    private val sessionStore: SessionStore,
    private val accounts: MockAccountStore,
    private val clock: Clock,
) : WorkspaceRepository {
    private val lock = Any()
    private val data = MockWorkspaceData(clock)

    override suspend fun getSpaces(): Result<List<Space>> =
        workspaceResult {
            val user = requireUser()
            data.spaces
                .filter { roleFor(it.id, user.id) != null }
                .map { spaceRead(it.id, user.id) }
                .sortedBy { it.name }
        }

    override suspend fun createSpace(name: String): Result<Space> =
        workspaceResult {
            val user = requireUser()
            val space = SpaceRecord(data.nextSpaceId++, validName(name), UUID.randomUUID().toString().take(12))
            data.spaces += space
            data.memberships += Membership(space.id, user.id, SpaceRole.Admin, LocalDateTime.now(clock))
            spaceRead(space.id, user.id)
        }

    override suspend fun renameSpace(
        spaceId: Int,
        name: String,
    ): Result<Space> =
        workspaceResult {
            val user = requireUser()
            require(membership(spaceId, user.id).role == SpaceRole.Admin)
            val index = data.spaces.indexOfFirst { it.id == spaceId }
            data.spaces[index] = data.spaces[index].copy(name = validName(name))
            spaceRead(spaceId, user.id)
        }

    override suspend fun deleteSpace(spaceId: Int): Result<Unit> =
        workspaceResult {
            require(membership(spaceId, requireUser().id).role == SpaceRole.Admin)
            deleteSpaceData(spaceId)
        }

    override suspend fun joinSpace(code: String): Result<Space> =
        workspaceResult {
            val user = requireUser()
            val space = data.spaces.firstOrNull { it.inviteCode == code.trim() } ?: error("Space not found")
            if (roleFor(space.id, user.id) == null) {
                data.memberships += Membership(space.id, user.id, SpaceRole.Visitor, LocalDateTime.now(clock))
                data.invitations.removeAll { it.spaceId == space.id && it.userId == user.id }
            }
            spaceRead(space.id, user.id)
        }

    override suspend fun leaveSpace(spaceId: Int): Result<Unit> =
        workspaceResult {
            leave(spaceId, requireUser().id)
        }

    override suspend fun regenerateInviteCode(spaceId: Int): Result<Space> =
        workspaceResult {
            val user = requireUser()
            require(membership(spaceId, user.id).role.canManage)
            val index = data.spaces.indexOfFirst { it.id == spaceId }
            data.spaces[index] = data.spaces[index].copy(inviteCode = UUID.randomUUID().toString().take(12))
            spaceRead(spaceId, user.id)
        }

    override suspend fun getBoardSettings(): Result<BoardSettings> = workspaceResult { boardFor(requireUser().id) }

    override suspend fun saveBoardSettings(settings: BoardSettings): Result<BoardSettings> =
        workspaceResult {
            val user = requireUser()
            val filtered =
                settings.copy(
                    spaceIds = settings.spaceIds.filter { roleFor(it, user.id) != null }.toSet(),
                )
            data.boards[user.id] = filtered
            filtered
        }

    override suspend fun getMembers(spaceId: Int): Result<List<SpaceMember>> =
        workspaceResult {
            membership(spaceId, requireUser().id)
            data.memberships
                .filter { it.spaceId == spaceId }
                .map(::memberRead)
                .sortedBy { it.fullName }
        }

    override suspend fun updateMemberRole(
        spaceId: Int,
        userId: Int,
        role: SpaceRole,
    ): Result<SpaceMember> =
        workspaceResult {
            val actor = membership(spaceId, requireUser().id)
            val target = membership(spaceId, userId)
            requireCanManage(actor, target, role)
            requireNotLastAdmin(target, role)
            val updated = target.copy(role = role)
            data.memberships[data.memberships.indexOf(target)] = updated
            memberRead(updated)
        }

    override suspend fun removeMember(
        spaceId: Int,
        userId: Int,
    ): Result<Unit> =
        workspaceResult {
            val user = requireUser()
            if (userId == user.id) {
                leave(spaceId, userId)
            } else {
                val actor = membership(spaceId, user.id)
                val target = membership(spaceId, userId)
                requireCanManage(actor, target, null)
                requireNotLastAdmin(target, null)
                detach(target)
            }
        }

    override suspend fun getInvitations(): Result<List<SpaceInvitation>> =
        workspaceResult {
            val user = requireUser()
            data.invitations
                .filter { it.userId == user.id }
                .sortedBy { it.createdAt }
                .map(::invitationRead)
        }

    override suspend fun getSpaceInvitations(spaceId: Int): Result<List<SpaceInvitation>> =
        workspaceResult {
            require(membership(spaceId, requireUser().id).role.canManage)
            data.invitations
                .filter { it.spaceId == spaceId }
                .sortedBy { it.createdAt }
                .map(::invitationRead)
        }

    override suspend fun invite(
        spaceId: Int,
        login: String,
        role: SpaceRole,
    ): Result<SpaceInvitation> =
        workspaceResult {
            val user = requireUser()
            requireCanManage(membership(spaceId, user.id), null, role)
            val normalized = login.trim().lowercase(Locale.ROOT)
            val invitee =
                accounts.users.value.firstOrNull { it.login == normalized && it.isActive }
                    ?: error("User not found")
            require(roleFor(spaceId, invitee.id) == null) { "Already a member" }
            require(
                data.invitations.none { it.spaceId == spaceId && it.userId == invitee.id },
            ) { "Already invited" }
            val invitation =
                InvitationRecord(
                    data.nextInvitationId++,
                    spaceId,
                    invitee.id,
                    role,
                    user.id,
                    LocalDateTime.now(clock),
                )
            data.invitations += invitation
            invitationRead(invitation)
        }

    override suspend fun cancelInvitation(
        spaceId: Int,
        invitationId: Int,
    ): Result<Unit> =
        workspaceResult {
            val actor = membership(spaceId, requireUser().id)
            val invitation =
                data.invitations.firstOrNull { it.id == invitationId && it.spaceId == spaceId }
                    ?: error("Invitation not found")
            requireCanManage(actor, null, invitation.role)
            data.invitations.remove(invitation)
            Unit
        }

    override suspend fun acceptInvitation(invitationId: Int): Result<Space> =
        workspaceResult {
            val user = requireUser()
            val invitation = ownInvitation(invitationId, user.id)
            if (roleFor(invitation.spaceId, user.id) == null) {
                data.memberships +=
                    Membership(invitation.spaceId, user.id, invitation.role, LocalDateTime.now(clock))
            }
            data.invitations.remove(invitation)
            spaceRead(invitation.spaceId, user.id)
        }

    override suspend fun declineInvitation(invitationId: Int): Result<Unit> =
        workspaceResult {
            data.invitations.remove(ownInvitation(invitationId, requireUser().id))
            Unit
        }

    override suspend fun updateProfile(draft: ProfileDraft): Result<CalendarUser> =
        workspaceResult {
            accounts.updateProfile(requireUser().id, draft).also(::updateSession)
        }

    override suspend fun getUsers(): Result<List<CalendarUser>> =
        workspaceResult {
            requireAdmin()
            accounts.users.value.sortedBy { it.fullName }
        }

    override suspend fun saveUser(
        userId: Int?,
        draft: ManagedUserDraft,
    ): Result<CalendarUser> =
        workspaceResult {
            val actor = requireAdmin()
            accounts.saveUser(userId, draft).also { if (it.id == actor.id) updateSession(it) }
        }

    override suspend fun deleteUser(userId: Int): Result<Unit> =
        workspaceResult {
            accounts.deleteUser(userId, requireAdmin().id)
            deleteUserData(userId)
        }

    internal fun roleFor(
        spaceId: Int,
        userId: Int,
    ): SpaceRole? =
        synchronized(lock) {
            data.memberships.firstOrNull { it.spaceId == spaceId && it.userId == userId }?.role
        }

    internal fun boardFor(userId: Int): BoardSettings =
        synchronized(lock) {
            val board = data.boards[userId] ?: BoardSettings(emptySet())
            board.copy(spaceIds = board.spaceIds.filter { roleFor(it, userId) != null }.toSet())
        }

    internal fun spaceName(spaceId: Int): String? =
        synchronized(lock) {
            data.spaces.firstOrNull { it.id == spaceId }?.name
        }

    internal fun detachmentsAfter(sequence: Int): List<WorkspaceDetachment> =
        synchronized(lock) { data.detachments.filter { it.sequence > sequence } }

    internal val deletedUserIds: Set<Int>
        get() = synchronized(lock) { data.deletedUsers.toSet() }

    private fun membership(
        spaceId: Int,
        userId: Int,
    ): Membership =
        data.memberships.firstOrNull { it.spaceId == spaceId && it.userId == userId } ?: error("Space not found")

    private fun spaceRead(
        spaceId: Int,
        userId: Int,
    ): Space {
        val space = data.spaces.first { it.id == spaceId }
        val role = membership(spaceId, userId).role
        return Space(
            id = space.id,
            name = space.name,
            myRole = role,
            memberCount = data.memberships.count { it.spaceId == spaceId },
            inviteCode = space.inviteCode.takeIf { role.canManage },
        )
    }

    private fun memberRead(member: Membership): SpaceMember {
        val user = accounts.users.value.first { it.id == member.userId }
        return SpaceMember(user.id, user.login, user.fullName, user.color, member.role, member.joinedAt)
    }

    private fun invitationRead(invitation: InvitationRecord): SpaceInvitation {
        val user = accounts.users.value.first { it.id == invitation.userId }
        return SpaceInvitation(
            invitation.id,
            invitation.spaceId,
            data.spaces.first { it.id == invitation.spaceId }.name,
            user.id,
            user.login,
            user.fullName,
            invitation.role,
            accounts.users.value
                .firstOrNull { it.id == invitation.invitedBy }
                ?.fullName,
            invitation.createdAt,
        )
    }

    private fun ownInvitation(
        invitationId: Int,
        userId: Int,
    ): InvitationRecord =
        data.invitations.firstOrNull { it.id == invitationId && it.userId == userId }
            ?: error("Invitation not found")

    private fun requireCanManage(
        actor: Membership,
        target: Membership?,
        role: SpaceRole?,
    ) {
        require(actor.role.canManage)
        require(actor.role == SpaceRole.Admin || (target?.role != SpaceRole.Admin && role != SpaceRole.Admin))
    }

    private fun requireNotLastAdmin(
        target: Membership,
        newRole: SpaceRole?,
    ) {
        require(
            target.role != SpaceRole.Admin || newRole == SpaceRole.Admin ||
                data.memberships.count { it.spaceId == target.spaceId && it.role == SpaceRole.Admin } > 1,
        ) {
            "A space needs at least one admin"
        }
    }

    private fun leave(
        spaceId: Int,
        userId: Int,
    ) {
        val target = membership(spaceId, userId)
        requireNotLastAdmin(target, null)
        detach(target)
    }

    private fun detach(member: Membership) {
        data.memberships.remove(member)
        data.detach(member.spaceId, member.userId)
    }

    private fun deleteSpaceData(spaceId: Int) {
        data.spaces.removeAll { it.id == spaceId }
        data.memberships.removeAll { it.spaceId == spaceId }
        data.invitations.removeAll { it.spaceId == spaceId }
        data.detach(spaceId, null)
    }

    private fun deleteUserData(userId: Int) {
        val memberships = data.memberships.filter { it.userId == userId }
        memberships.forEach { member ->
            val others = data.memberships.filter { it.spaceId == member.spaceId && it.userId != userId }
            data.memberships.remove(member)
            if (others.isEmpty()) {
                deleteSpaceData(member.spaceId)
            } else if (member.role == SpaceRole.Admin && others.none { it.role == SpaceRole.Admin }) {
                val sorted = others.sortedBy { it.joinedAt }
                val successor = sorted.firstOrNull { it.role == SpaceRole.Moderator } ?: sorted.first()
                data.memberships[data.memberships.indexOf(successor)] = successor.copy(role = SpaceRole.Admin)
            }
        }
        data.invitations.removeAll { it.userId == userId }
        data.invitations.replaceAll { if (it.invitedBy == userId) it.copy(invitedBy = null) else it }
        data.boards.remove(userId)
        data.deletedUsers += userId
    }

    private fun validName(name: String): String = name.trim().also { require(it.length in 1..100) }

    private fun requireUser(): CalendarUser {
        val userId = sessionStore.getUser()?.id ?: throw AuthenticationException()
        return accounts.users.value.firstOrNull { it.id == userId && it.isActive }
            ?: throw AuthenticationException()
    }

    private fun requireAdmin(): CalendarUser = requireUser().also { require(it.role == UserRole.ADMIN) }

    private fun updateSession(user: CalendarUser) {
        if (user.isActive) {
            sessionStore.save(
                sessionStore.getToken() ?: "mock-session",
                user,
            )
        } else {
            sessionStore.clear()
        }
    }

    private suspend fun <Value> workspaceResult(block: () -> Value): Result<Value> =
        repositoryResult { synchronized(lock, block) }
}
