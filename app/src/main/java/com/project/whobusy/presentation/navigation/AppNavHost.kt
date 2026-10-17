package com.project.whobusy.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.project.whobusy.presentation.calendar.ui.CalendarRoot
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarEffect
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import com.project.whobusy.presentation.calendars.ui.CalendarsRoot
import com.project.whobusy.presentation.event.ui.EventDetailsRoot
import com.project.whobusy.presentation.event.ui.EventEditorRoot
import com.project.whobusy.presentation.login.ui.LoginRoot
import com.project.whobusy.presentation.profile.ui.ProfileRoot

@Composable
fun AppNavHost(userId: Int?) {
    key(userId) {
        val backStack = rememberNavBackStack(if (userId == null) ScreenTypes.Login else ScreenTypes.Calendar)
        val viewModel =
            if (userId != null) {
                val owner = rememberViewModelStoreOwner()
                hiltViewModel<CalendarViewModel>(viewModelStoreOwner = owner)
            } else {
                null
            }
        val onBack: () -> Unit = {
            if (viewModel?.state?.value?.isSaving != true && backStack.size > 1) {
                viewModel?.onEvent(CalendarCommand.ClearError)
                backStack.removeLastOrNull()
            }
        }
        LaunchedEffect(viewModel) {
            viewModel?.effects?.collect { effect ->
                when (effect) {
                    is CalendarEffect.OpenEvent -> backStack.navigateTo(ScreenTypes.EventDetails(effect.eventId))
                    is CalendarEffect.CreateEvent -> {
                        viewModel.onEvent(CalendarCommand.ClearError)
                        backStack.navigateTo(ScreenTypes.EventEditor(effect.date))
                    }
                    is CalendarEffect.EditEvent -> {
                        viewModel.onEvent(CalendarCommand.ClearError)
                        backStack.navigateTo(
                            ScreenTypes.EventEditor(
                                date =
                                    viewModel.state.value.selectedDate
                                        .toString(),
                                eventId = effect.eventId,
                            ),
                        )
                    }
                    CalendarEffect.OpenCalendars -> backStack.navigateToTab(ScreenTypes.Calendars)
                    CalendarEffect.EventSaved, CalendarEffect.EventDeleted -> {
                        while (backStack.size > 1 && backStack.last() !is ScreenTypes.BottomBarNavigatable) {
                            backStack.removeLastOrNull()
                        }
                    }
                }
            }
        }
        Box(Modifier.fillMaxSize()) {
            NavDisplay(
                modifier = Modifier.fillMaxSize(),
                backStack = backStack,
                onBack = onBack,
                entryDecorators =
                    listOf(
                        rememberSaveableStateHolderNavEntryDecorator(),
                        rememberViewModelStoreNavEntryDecorator(),
                    ),
                entryProvider =
                    entryProvider {
                        entry<ScreenTypes.Login> { LoginRoot() }
                        entry<ScreenTypes.Calendar> { CalendarRoot(checkNotNull(viewModel)) }
                        entry<ScreenTypes.Calendars> { CalendarsRoot(checkNotNull(viewModel)) }
                        entry<ScreenTypes.Profile> { ProfileRoot(checkNotNull(viewModel)) }
                        entry<ScreenTypes.EventDetails> { route ->
                            EventDetailsRoot(route.eventId, onBack, checkNotNull(viewModel))
                        }
                        entry<ScreenTypes.EventEditor> { route ->
                            EventEditorRoot(route.date, route.eventId, onBack, checkNotNull(viewModel))
                        }
                    },
            )
            val currentScreen = backStack.lastOrNull()
            if (currentScreen is ScreenTypes.BottomBarNavigatable) {
                AppBottomBar(
                    currentScreen = currentScreen,
                    onSelect = { backStack.navigateToTab(it) },
                )
            }
        }
    }
}
