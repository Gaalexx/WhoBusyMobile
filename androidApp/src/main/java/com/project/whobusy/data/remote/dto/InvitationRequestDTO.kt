package com.project.whobusy.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class InvitationRequestDTO(val login: String, val role: SpaceRoleDTO)
