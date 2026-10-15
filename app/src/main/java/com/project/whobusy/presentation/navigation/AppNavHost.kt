package com.project.whobusy.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.project.whobusy.presentation.calendar.ui.CalendarRoot
import com.project.whobusy.presentation.login.ui.LoginRoot

@Composable
fun AppNavHost(userId: Int?) {
    key(userId) {
        val destination = if (userId == null) AppDestination.Login else AppDestination.Calendar
        val backStack = rememberNavBackStack(destination)
        NavDisplay(
            backStack = backStack,
            onBack = {},
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            entryProvider = entryProvider {
                entry<AppDestination.Login> { LoginRoot() }
                entry<AppDestination.Calendar> { CalendarRoot() }
            },
        )
    }
}
