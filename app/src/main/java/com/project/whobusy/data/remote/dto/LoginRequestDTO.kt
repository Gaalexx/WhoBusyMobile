package com.project.whobusy.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDTO(
    val login: String,
    val password: String,
)
