package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.ManagedUserDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManagedUserCreateRequestDTO(
    val login: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String?,
    val role: String,
    val color: String,
    val password: String,
) {
    companion object {
        fun fromDomain(draft: ManagedUserDraft) =
            ManagedUserCreateRequestDTO(
                login = draft.login.trim().lowercase(),
                firstName = draft.firstName.trim(),
                lastName = draft.lastName.trim(),
                email = draft.email?.trim()?.takeIf { it.isNotEmpty() },
                role = draft.role.name.lowercase(),
                color = draft.color,
                password = requireNotNull(draft.password) { "A new user requires a password" },
            )
    }
}
