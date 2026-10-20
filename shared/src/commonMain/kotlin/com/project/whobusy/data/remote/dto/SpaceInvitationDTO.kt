package com.project.whobusy.data.remote.dto

import com.project.whobusy.time.LocalDateTime

import com.project.whobusy.domain.model.SpaceInvitation
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpaceInvitationDTO(
    val id: Int,
    @SerialName("space_id") val spaceId: Int,
    @SerialName("space_name") val spaceName: String,
    @SerialName("user_id") val userId: Int,
    val login: String,
    @SerialName("full_name") val fullName: String,
    val role: SpaceRoleDTO,
    @SerialName("invited_by_name") val invitedByName: String? = null,
    @SerialName("created_at") val createdAt: String,
) {
    fun toDomain() =
        SpaceInvitation(
            id, spaceId, spaceName, userId, login, fullName, role.toDomain(), invitedByName,
            LocalDateTime.parse(createdAt),
        )
}
