package com.project.whobusy.domain.model

data class CalendarUser(
    val id: Int,
    val email: String?,
    val fullName: String,
    val role: UserRole,
    val color: String,
    val isActive: Boolean,
    val login: String = email?.substringBefore('@').orEmpty(),
    val firstName: String = fullName.substringBefore(' '),
    val lastName: String = fullName.substringAfter(' ', ""),
)
