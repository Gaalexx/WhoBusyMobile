package com.project.whobusy.domain.model

data class Space(
    val id: Int,
    val name: String,
    val myRole: SpaceRole,
    val memberCount: Int,
    val inviteCode: String? = null,
)
