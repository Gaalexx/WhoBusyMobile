package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.SpaceRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class SpaceRoleDTO {
    @SerialName("admin")
    Admin,

    @SerialName("moderator")
    Moderator,

    @SerialName("actor")
    Actor,

    @SerialName("visitor")
    Visitor,
    ;

    fun toDomain(): SpaceRole = SpaceRole.valueOf(name)

    companion object {
        fun fromDomain(role: SpaceRole): SpaceRoleDTO = valueOf(role.name)
    }
}
