package com.project.whobusy.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AppDestination : NavKey {
    @Serializable
    data object Login : AppDestination

    @Serializable
    data object Calendar : AppDestination
}
