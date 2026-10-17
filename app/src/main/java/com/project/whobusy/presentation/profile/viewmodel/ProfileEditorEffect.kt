package com.project.whobusy.presentation.profile.viewmodel

sealed interface ProfileEditorEffect {
    data object Saved : ProfileEditorEffect
}
