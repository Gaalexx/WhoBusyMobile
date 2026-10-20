package com.project.whobusy.domain.model

import com.project.whobusy.time.LocalDateTime

data class SpaceMember(
    val userId: Int,
    val login: String,
    val fullName: String,
    val color: String,
    val role: SpaceRole,
    val joinedAt: LocalDateTime,
)
