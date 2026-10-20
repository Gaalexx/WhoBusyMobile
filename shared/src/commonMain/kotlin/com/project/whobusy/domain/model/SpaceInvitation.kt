package com.project.whobusy.domain.model

import com.project.whobusy.time.LocalDateTime

data class SpaceInvitation(
    val id: Int,
    val spaceId: Int,
    val spaceName: String,
    val userId: Int,
    val login: String,
    val fullName: String,
    val role: SpaceRole,
    val invitedByName: String?,
    val createdAt: LocalDateTime,
)
