package com.project.whobusy.presentation.spaces.viewmodel

sealed interface SpacesEffect {
    data class OpenSpace(
        val spaceId: Int,
    ) : SpacesEffect

    data class CopyCode(
        val value: String,
    ) : SpacesEffect

    data object MemberSaved : SpacesEffect

    data object Left : SpacesEffect

    data object Deleted : SpacesEffect
}
