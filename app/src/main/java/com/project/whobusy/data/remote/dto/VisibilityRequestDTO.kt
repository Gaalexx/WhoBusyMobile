package com.project.whobusy.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VisibilityRequestDTO(
    @SerialName("visible_user_ids") val visibleUserIds: List<Int>,
)
