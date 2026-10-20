package com.project.whobusy.domain.model

data class ManagedUserDraft(
    val login: String,
    val firstName: String,
    val lastName: String,
    val email: String?,
    val role: UserRole,
    val color: String,
    val isActive: Boolean,
    val password: String? = null,
)
