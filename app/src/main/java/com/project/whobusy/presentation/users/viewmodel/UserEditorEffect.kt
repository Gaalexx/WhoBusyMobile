package com.project.whobusy.presentation.users.viewmodel

sealed interface UserEditorEffect {
    data object Saved : UserEditorEffect

    data object Deleted : UserEditorEffect
}
