package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.Space
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SpaceDTO(
    val id: Int,
    val name: String,
    @SerialName("my_role") val myRole: SpaceRoleDTO,
    @SerialName("member_count") val memberCount: Int,
    @SerialName("invite_code") val inviteCode: String? = null,
) {
    fun toDomain() = Space(id, name, myRole.toDomain(), memberCount, inviteCode)
}
