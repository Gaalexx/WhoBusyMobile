package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.BoardSettings
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BoardSettingsDTO(
    @SerialName("space_ids") val spaceIds: List<Int>,
    @SerialName("show_own") val showOwn: Boolean = true,
) {
    fun toDomain() = BoardSettings(spaceIds.toSet(), showOwn)

    companion object {
        fun fromDomain(settings: BoardSettings) = BoardSettingsDTO(settings.spaceIds.sorted(), settings.showOwn)
    }
}
