package com.project.whobusy.domain.model

data class ProfileDraft(
    val firstName: String,
    val lastName: String,
    val email: String?,
    val color: String,
    val password: String? = null,
    val currentPassword: String? = null,
)
