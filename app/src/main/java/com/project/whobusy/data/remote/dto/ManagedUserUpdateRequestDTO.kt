package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.ManagedUserDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ManagedUserUpdateRequestDTO(
    val login: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String?,
    val role: String,
    val color: String,
    @SerialName("is_active") val isActive: Boolean,
    val password: String? = null,
) {
    companion object {
        fun fromDomain(draft: ManagedUserDraft) =
            ManagedUserUpdateRequestDTO(
                login = draft.login.trim().lowercase(),
                firstName = draft.firstName.trim(),
                lastName = draft.lastName.trim(),
                email = draft.email?.trim()?.takeIf { it.isNotEmpty() },
                role = draft.role.name.lowercase(),
                color = draft.color,
                isActive = draft.isActive,
                password = draft.password?.takeIf { it.isNotEmpty() },
            )
    }
}
