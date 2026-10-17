package com.project.whobusy.data.remote.dto

import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EventShareDTO(
    @SerialName("space_id") val spaceId: Int,
    val visibility: String,
) {
    fun toDomain() =
        EventShare(
            spaceId = spaceId,
            visibility = when (visibility) {
                "actors" -> EventVisibility.Actors
                "busy" -> EventVisibility.Busy
                else -> error("Unsupported event visibility: $visibility")
            },
        )

    companion object {
        fun fromDomain(share: EventShare) = EventShareDTO(share.spaceId, share.visibility.name.lowercase())
    }
}
