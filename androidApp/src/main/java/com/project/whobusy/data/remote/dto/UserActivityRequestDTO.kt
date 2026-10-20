package com.project.whobusy.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserActivityRequestDTO(@SerialName("is_active") val isActive: Boolean)
