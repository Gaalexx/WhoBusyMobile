package com.project.whobusy.domain.model

enum class SpaceRole {
    Admin,
    Moderator,
    Actor,
    Visitor,
    ;

    val canManage: Boolean
        get() = this == Admin || this == Moderator

    val canCreateEvents: Boolean
        get() = true
}
