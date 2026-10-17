package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.RegistrationDraft
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RegistrationRequestDTO(
    val login: String,
    val password: String,
    @SerialName("first_name") val firstName: String,
    @SerialName("last_name") val lastName: String,
    val email: String?,
) {
    companion object {
        fun fromDomain(draft: RegistrationDraft) = RegistrationRequestDTO(
            login = draft.login.trim(),
            password = draft.password,
            firstName = draft.firstName.trim(),
            lastName = draft.lastName.trim(),
            email = draft.email?.trim()?.takeIf { it.isNotEmpty() },
        )
    }
}
