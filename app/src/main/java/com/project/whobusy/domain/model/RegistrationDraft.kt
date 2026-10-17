package com.project.whobusy.domain.model

data class RegistrationDraft(
    val login: String,
    val firstName: String,
    val lastName: String,
    val password: String,
    val email: String? = null,
)
