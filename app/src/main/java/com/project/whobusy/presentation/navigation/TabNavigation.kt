package com.project.whobusy.presentation.navigation

import androidx.navigation3.runtime.NavKey

internal fun MutableList<NavKey>.navigateToTab(destination: ScreenTypes) {
    require(destination is ScreenTypes.BottomBarNavigatable)
    if (lastOrNull() == destination) return
    while (size > 1) removeLastOrNull()
    if (destination != ScreenTypes.Calendar) add(destination)
}

internal fun MutableList<NavKey>.navigateTo(destination: ScreenTypes) {
    if (lastOrNull() != destination) add(destination)
}
