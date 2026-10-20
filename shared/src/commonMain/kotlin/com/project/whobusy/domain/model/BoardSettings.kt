package com.project.whobusy.domain.model

data class BoardSettings(
    val spaceIds: Set<Int>,
    val showOwn: Boolean = true,
)
