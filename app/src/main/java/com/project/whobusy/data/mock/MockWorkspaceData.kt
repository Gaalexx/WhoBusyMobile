package com.project.whobusy.data.mock

import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.SpaceRole
import java.time.Clock
import java.time.LocalDateTime

internal class MockWorkspaceData(
    clock: Clock,
) {
    val spaces =
        mutableListOf(
            SpaceRecord(1, "Команда WhoBusy", "WBTEAM2026"),
            SpaceRecord(2, "Семья", "WBFAMILY2026"),
            SpaceRecord(3, "Проект", "WBPROJECT2026"),
            SpaceRecord(4, "Друзья", "WBFRIENDS2026"),
        )
    private val joinedAt = LocalDateTime.now(clock).minusMonths(1)
    val memberships =
        mutableListOf(
            Membership(1, 1, SpaceRole.Admin, joinedAt),
            Membership(1, 2, SpaceRole.Moderator, joinedAt.plusDays(1)),
            Membership(1, 3, SpaceRole.Actor, joinedAt.plusDays(2)),
            Membership(1, 4, SpaceRole.Visitor, joinedAt.plusDays(3)),
            Membership(2, 2, SpaceRole.Admin, joinedAt),
            Membership(2, 1, SpaceRole.Actor, joinedAt.plusDays(1)),
            Membership(2, 4, SpaceRole.Actor, joinedAt.plusDays(2)),
            Membership(3, 3, SpaceRole.Admin, joinedAt),
            Membership(3, 2, SpaceRole.Moderator, joinedAt.plusDays(1)),
            Membership(3, 1, SpaceRole.Visitor, joinedAt.plusDays(2)),
            Membership(4, 4, SpaceRole.Admin, joinedAt),
            Membership(4, 2, SpaceRole.Actor, joinedAt.plusDays(1)),
        )
    val invitations =
        mutableListOf(
            InvitationRecord(1, 4, 1, SpaceRole.Actor, 4, LocalDateTime.now(clock).minusHours(2)),
        )
    val boards = mutableMapOf(1 to BoardSettings(setOf(1, 2)))
    val detachments = mutableListOf<WorkspaceDetachment>()
    val deletedUsers = mutableSetOf<Int>()
    var nextSpaceId = 5
    var nextInvitationId = 2

    fun detach(
        spaceId: Int,
        userId: Int?,
    ) {
        detachments += WorkspaceDetachment(detachments.size + 1, spaceId, userId)
    }

    data class SpaceRecord(
        val id: Int,
        val name: String,
        val inviteCode: String,
    )

    data class Membership(
        val spaceId: Int,
        val userId: Int,
        val role: SpaceRole,
        val joinedAt: LocalDateTime,
    )

    data class InvitationRecord(
        val id: Int,
        val spaceId: Int,
        val userId: Int,
        val role: SpaceRole,
        val invitedBy: Int?,
        val createdAt: LocalDateTime,
    )
}
