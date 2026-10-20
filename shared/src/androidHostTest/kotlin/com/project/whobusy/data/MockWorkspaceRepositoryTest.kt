package com.project.whobusy.data

import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.SpaceRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import com.project.whobusy.time.Clock
import com.project.whobusy.time.Instant
import com.project.whobusy.time.ZoneOffset

class MockWorkspaceRepositoryTest {
    private val accounts = MockAccountStore()
    private val session = SessionStore()
    private val clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC)
    private val workspace = MockWorkspaceRepository(session, accounts, clock)

    @Test
    fun workspaceRequiresActiveAuthenticatedAccount() =
        runBlocking {
            assertTrue(workspace.getSpaces().exceptionOrNull() is AuthenticationException)
            assertTrue(workspace.getInvitations().exceptionOrNull() is AuthenticationException)
            assertTrue(workspace.getBoardSettings().exceptionOrNull() is AuthenticationException)
        }

    @Test
    fun globalAdministratorHasNoImplicitSpaceMembership() =
        runBlocking {
            signIn(100)

            assertTrue(workspace.getSpaces().getOrThrow().isEmpty())
            assertTrue(workspace.getMembers(1).isFailure)
            assertTrue(workspace.renameSpace(1, "Другой проект").isFailure)
            assertTrue(workspace.getUsers().getOrThrow().isNotEmpty())
        }

    @Test
    fun onlyManagersSeeInviteCodeAndEveryMemberCanCreatePlans() =
        runBlocking {
            signIn(1)
            val spaces = workspace.getSpaces().getOrThrow().associateBy { it.id }

            assertEquals("WBTEAM2026", spaces.getValue(1).inviteCode)
            assertNull(spaces.getValue(2).inviteCode)
            assertNull(spaces.getValue(3).inviteCode)
            assertTrue(SpaceRole.entries.all { it.canCreateEvents })
            assertTrue(SpaceRole.Admin.canManage)
            assertTrue(SpaceRole.Moderator.canManage)
            assertFalse(SpaceRole.Actor.canManage)
            assertFalse(SpaceRole.Visitor.canManage)
        }

    @Test
    fun lastAdministratorCannotLeaveRemoveOrDemoteThemself() =
        runBlocking {
            signIn(1)

            assertTrue(workspace.leaveSpace(1).isFailure)
            assertTrue(workspace.removeMember(1, 1).isFailure)
            assertTrue(workspace.updateMemberRole(1, 1, SpaceRole.Actor).isFailure)
            assertEquals(SpaceRole.Admin, workspace.roleFor(1, 1))
            workspace.updateMemberRole(1, 2, SpaceRole.Admin).getOrThrow()
            workspace.leaveSpace(1).getOrThrow()
            assertNull(workspace.roleFor(1, 1))
            assertEquals(SpaceRole.Admin, workspace.roleFor(1, 2))
        }

    @Test
    fun moderatorCanManageNonAdminsButCannotGrantOrManageAdminRole() =
        runBlocking {
            signIn(2)

            workspace.updateMemberRole(1, 4, SpaceRole.Actor).getOrThrow()
            assertTrue(workspace.updateMemberRole(1, 1, SpaceRole.Actor).isFailure)
            assertTrue(workspace.updateMemberRole(1, 4, SpaceRole.Admin).isFailure)
            assertTrue(workspace.removeMember(1, 1).isFailure)
            assertTrue(workspace.invite(1, "admin", SpaceRole.Admin).isFailure)
            assertTrue(workspace.renameSpace(1, "Переименовано").isFailure)
            assertTrue(workspace.deleteSpace(1).isFailure)
        }

    @Test
    fun invitationRequiresAcceptanceAndCannotBeReadOrAcceptedByAnotherAccount() =
        runBlocking {
            signIn(1)
            val invitation = workspace.invite(1, "  ADMIN ", SpaceRole.Moderator).getOrThrow()

            assertNull(workspace.roleFor(1, 100))
            assertTrue(workspace.invite(1, "admin", SpaceRole.Actor).isFailure)
            assertTrue(workspace.acceptInvitation(invitation.id).isFailure)
            assertFalse(workspace.getInvitations().getOrThrow().any { it.id == invitation.id })
            signIn(100)
            assertEquals(
                invitation.id,
                workspace
                    .getInvitations()
                    .getOrThrow()
                    .single()
                    .id,
            )
            val joined = workspace.acceptInvitation(invitation.id).getOrThrow()
            assertEquals(SpaceRole.Moderator, joined.myRole)
            assertTrue(workspace.getInvitations().getOrThrow().isEmpty())
            assertTrue(workspace.acceptInvitation(invitation.id).isFailure)
        }

    @Test
    fun joiningByCodeGrantsVisitorAndConsumesPendingInvitation() =
        runBlocking {
            signIn(1)

            assertEquals(
                4,
                workspace
                    .getInvitations()
                    .getOrThrow()
                    .single()
                    .spaceId,
            )
            val joined = workspace.joinSpace(" WBFRIENDS2026 ").getOrThrow()

            assertEquals(SpaceRole.Visitor, joined.myRole)
            assertTrue(workspace.getInvitations().getOrThrow().isEmpty())
            assertEquals(joined.memberCount, workspace.joinSpace("WBFRIENDS2026").getOrThrow().memberCount)
            assertTrue(workspace.joinSpace("invalid").isFailure)
        }

    @Test
    fun inviteeMayDeclineAndManagersMayCancelPendingInvitations() =
        runBlocking {
            signIn(1)
            workspace
                .declineInvitation(
                    workspace
                        .getInvitations()
                        .getOrThrow()
                        .single()
                        .id,
                ).getOrThrow()
            assertNull(workspace.roleFor(4, 1))

            val invitation = workspace.invite(1, "admin", SpaceRole.Actor).getOrThrow()
            signIn(2)
            workspace.cancelInvitation(1, invitation.id).getOrThrow()
            signIn(100)
            assertTrue(workspace.getInvitations().getOrThrow().isEmpty())
        }

    @Test
    fun boardDropsUnknownMembershipsOnSaveAndAfterLeavingSpace() =
        runBlocking {
            signIn(1)
            val saved = workspace.saveBoardSettings(BoardSettings(setOf(2, 3, 999), showOwn = false)).getOrThrow()

            assertEquals(BoardSettings(setOf(2, 3), showOwn = false), saved)
            workspace.leaveSpace(2).getOrThrow()
            assertEquals(BoardSettings(setOf(3), showOwn = false), workspace.getBoardSettings().getOrThrow())
            signIn(2)
            assertEquals(BoardSettings(emptySet()), workspace.getBoardSettings().getOrThrow())
            signIn(1)
            assertEquals(BoardSettings(setOf(3), showOwn = false), workspace.getBoardSettings().getOrThrow())
        }

    @Test
    fun deletingSpaceRemovesMembershipsInvitationsAndBoardSelection() =
        runBlocking {
            signIn(1)
            val created = workspace.createSpace("  Временный проект  ").getOrThrow()
            assertEquals("Временный проект", created.name)
            assertEquals(SpaceRole.Admin, created.myRole)
            workspace.invite(created.id, "admin", SpaceRole.Actor).getOrThrow()
            workspace.saveBoardSettings(BoardSettings(setOf(created.id))).getOrThrow()

            workspace.deleteSpace(created.id).getOrThrow()

            assertNull(workspace.roleFor(created.id, 1))
            assertTrue(
                workspace
                    .getBoardSettings()
                    .getOrThrow()
                    .spaceIds
                    .isEmpty(),
            )
            assertEquals(created.id, workspace.detachmentsAfter(0).single().spaceId)
            assertNull(workspace.detachmentsAfter(0).single().userId)
            signIn(100)
            assertTrue(workspace.getInvitations().getOrThrow().isEmpty())
        }

    @Test
    fun repeatedLeaveAfterRejoiningProducesSeparateReconciliationChanges() =
        runBlocking {
            signIn(1)
            workspace.leaveSpace(2).getOrThrow()
            val first = workspace.detachmentsAfter(0).single()
            workspace.joinSpace("WBFAMILY2026").getOrThrow()
            workspace.leaveSpace(2).getOrThrow()
            val second = workspace.detachmentsAfter(first.sequence).single()

            assertEquals(first.spaceId, second.spaceId)
            assertEquals(first.userId, second.userId)
            assertTrue(second.sequence > first.sequence)
        }

    @Test
    fun deletingAnAccountTransfersLastAdministratorRoleToModerator() =
        runBlocking {
            signIn(100)
            workspace.deleteUser(1).getOrThrow()

            assertEquals(SpaceRole.Admin, workspace.roleFor(1, 2))
            assertNull(workspace.roleFor(1, 1))
            assertEquals(setOf(1), workspace.deletedUserIds)
            assertTrue(workspace.getUsers().getOrThrow().none { it.id == 1 })
            assertTrue(workspace.deleteUser(100).isFailure)
        }

    private fun signIn(userId: Int) {
        session.save("mock-session", accounts.users.value.first { it.id == userId })
    }
}
