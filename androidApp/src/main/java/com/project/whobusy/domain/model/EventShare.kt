package com.project.whobusy.domain.model

enum class EventVisibility { Actors, Busy }

data class EventShare(
    val spaceId: Int,
    val visibility: EventVisibility,
)
