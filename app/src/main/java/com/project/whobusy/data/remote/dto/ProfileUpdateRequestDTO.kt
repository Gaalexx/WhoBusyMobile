package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.ProfileDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProfileUpdateRequestDTO(
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String?,
    val color: String,
    val password: String? = null,
    @SerialName("current_password") val currentPassword: String? = null,
) {
    companion object {
        fun fromDomain(draft: ProfileDraft) =
            ProfileUpdateRequestDTO(
                firstName = draft.firstName.trim(),
                lastName = draft.lastName.trim(),
                email = draft.email?.trim()?.takeIf { it.isNotEmpty() },
                color = draft.color,
                password = draft.password?.takeIf { it.isNotEmpty() },
                currentPassword = draft.currentPassword?.takeIf { it.isNotEmpty() },
            )
    }
}
