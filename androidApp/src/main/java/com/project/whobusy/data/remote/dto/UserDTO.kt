package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Int,
    val login: String,
    val email: String?,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    @SerialName("full_name") val fullName: String,
    val role: String,
    val color: String,
    @SerialName("is_active") val isActive: Boolean,
) {
    fun toDomain() =
        CalendarUser(
            id = id,
            email = email,
            fullName = fullName,
            role =
                when (role) {
                    "admin" -> UserRole.ADMIN
                    "user" -> UserRole.USER
                    else -> error("Unsupported user role: $role")
                },
            color = color,
            isActive = isActive,
            login = login,
            firstName = firstName,
            lastName = lastName,
        )
}
