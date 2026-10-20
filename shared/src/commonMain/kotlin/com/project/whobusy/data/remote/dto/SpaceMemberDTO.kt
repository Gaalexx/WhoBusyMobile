package com.project.whobusy.data.remote.dto

import com.project.whobusy.time.LocalDateTime

import com.project.whobusy.domain.model.SpaceMember
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpaceMemberDTO(
    @SerialName("user_id") val userId: Int,
    val login: String,
    @SerialName("full_name") val fullName: String,
    val color: String,
    val role: SpaceRoleDTO,
    @SerialName("joined_at") val joinedAt: String,
) {
    fun toDomain() = SpaceMember(userId, login, fullName, color, role.toDomain(), LocalDateTime.parse(joinedAt))
}
