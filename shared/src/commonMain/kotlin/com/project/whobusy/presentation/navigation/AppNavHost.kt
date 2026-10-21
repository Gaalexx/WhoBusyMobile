package com.project.whobusy.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.rememberViewModelStoreOwner
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.presentation.board.ui.BoardSettingsRoot
import com.project.whobusy.presentation.calendar.ui.CalendarRoot
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.viewmodel.CalendarCommand
import com.project.whobusy.presentation.calendar.viewmodel.CalendarViewModel
import com.project.whobusy.presentation.desktop.ui.DesktopNavigationRail
import com.project.whobusy.presentation.desktop.ui.DesktopSpaceSidebar
import com.project.whobusy.presentation.event.ui.EventDatesRoot
import com.project.whobusy.presentation.event.ui.EventDetailsRoot
import com.project.whobusy.presentation.event.ui.EventEditorRoot
import com.project.whobusy.presentation.event.ui.EventUnavailableScreen
import com.project.whobusy.presentation.event.ui.EventVisibilityRoot
import com.project.whobusy.presentation.event.ui.SpaceVisibilityRoot
import com.project.whobusy.presentation.event.viewmodel.EventEditorViewModel
import com.project.whobusy.presentation.login.ui.LoginRoot
import com.project.whobusy.presentation.profile.ui.ProfileEditorRoot
import com.project.whobusy.presentation.profile.ui.ProfileRoot
import com.project.whobusy.presentation.register.ui.RegistrationRoot
import com.project.whobusy.presentation.spaces.ui.CreateSpaceRoot
import com.project.whobusy.presentation.spaces.ui.InvitationsRoot
import com.project.whobusy.presentation.spaces.ui.InviteMemberRoot
import com.project.whobusy.presentation.spaces.ui.JoinSpaceRoot
import com.project.whobusy.presentation.spaces.ui.LeaveSpaceRoot
import com.project.whobusy.presentation.spaces.ui.MemberRoleRoot
import com.project.whobusy.presentation.spaces.ui.SpaceDetailsRoot
import com.project.whobusy.presentation.spaces.ui.SpaceMembersRoot
import com.project.whobusy.presentation.spaces.ui.SpaceSettingsRoot
import com.project.whobusy.presentation.spaces.ui.SpacesRoot
import com.project.whobusy.presentation.users.ui.UserEditorRoot
import com.project.whobusy.presentation.users.ui.UsersRoot
import dev.zacsweers.metrox.viewmodel.assistedMetroViewModel

@Composable
fun AppNavHost(userId: Int?, desktopLayout: Boolean = false) {
    key(userId) {
        val backStack = rememberNavBackStack(
            navigationSavedStateConfiguration,
            if (userId == null) ScreenTypes.Login else ScreenTypes.Calendar,
        )
        val owner = if (userId != null) rememberViewModelStoreOwner() else null
        val calendar = owner?.let { assistedMetroViewModel<CalendarViewModel>(viewModelStoreOwner = it) }
        val editor = owner?.let { assistedMetroViewModel<EventEditorViewModel>(viewModelStoreOwner = it) }
        var spaceNavigationBlocked by remember { mutableStateOf(false) }
        var profileNavigationBlocked by remember { mutableStateOf(false) }
        LaunchedEffect(calendar) {
            if (desktopLayout) calendar?.onEvent(CalendarCommand.SelectMode(CalendarMode.Week))
        }
        val refresh = {
            calendar?.onEvent(CalendarCommand.Refresh)
            Unit
        }
        val onBack: () -> Unit = {
            val calendarState = calendar?.state?.value
            val editorState = editor?.state?.value
            if (!spaceNavigationBlocked && !profileNavigationBlocked &&
                calendarState?.isSaving != true && calendarState?.result == null &&
                editorState?.isSaving != true && editorState?.isSaved != true &&
                backStack.size > 1
            ) {
                if (backStack.lastOrNull() is ScreenTypes.EventEditor) editor?.reset()
                calendar?.onEvent(CalendarCommand.ClearError)
                backStack.removeLastOrNull()
            }
        }
        val finishEvent: () -> Unit = {
            editor?.reset()
            while (backStack.size > 1 && (backStack.last() as? ScreenTypes)?.isEventRoute() == true) {
                backStack.removeLastOrNull()
            }
            refresh()
        }
        val finishSpace: () -> Unit = {
            while (backStack.size > 1 &&
                backStack.last() !is ScreenTypes.BottomBarNavigatable
            ) {
                backStack.removeLastOrNull()
            }
            refresh()
        }
        val openSpace: (Int) -> Unit = { id ->
            if (!spaceNavigationBlocked && !profileNavigationBlocked &&
                calendar?.state?.value?.isSaving != true && calendar?.state?.value?.result == null &&
                editor?.state?.value?.isSaving != true && editor?.state?.value?.isSaved != true
            ) {
                if (backStack.last() in listOf(ScreenTypes.CreateSpace, ScreenTypes.JoinSpace, ScreenTypes.Invitations)) {
                    backStack.removeLastOrNull()
                }
                backStack.navigateTo(ScreenTypes.SpaceDetails(id))
                refresh()
            }
        }
        val openEvent: (CalendarEvent) -> Unit = { event ->
            checkNotNull(calendar).rememberEvent(event)
            backStack.navigateTo(event.detailsRoute())
        }
        val currentScreen = backStack.lastOrNull()
        val desktop = desktopLayout && userId != null
        val railNavigationAllowed = currentScreen is ScreenTypes.BottomBarNavigatable ||
            currentScreen is ScreenTypes.SpaceDetails || currentScreen is ScreenTypes.EventDetails ||
            currentScreen is ScreenTypes.EventEditor
        val selectTab: (ScreenTypes) -> Unit = {
            if ((!desktop || railNavigationAllowed) && !spaceNavigationBlocked && !profileNavigationBlocked &&
                calendar?.state?.value?.isSaving != true && calendar?.state?.value?.result == null &&
                editor?.state?.value?.isSaving != true && editor?.state?.value?.isSaved != true
            ) {
                backStack.navigateToTab(it)
                if (it == ScreenTypes.Calendar) refresh()
            }
        }
        Row(Modifier.fillMaxSize()) {
            if (desktop) {
                DesktopNavigationRail(
                    currentScreen as? ScreenTypes, calendar?.state?.collectAsStateWithLifecycle()?.value?.user, selectTab,
                    enabled = railNavigationAllowed && !spaceNavigationBlocked && !profileNavigationBlocked,
                )
            }
            Box(Modifier.weight(1f).fillMaxHeight()) {
                NavDisplay(
                    modifier = when {
                        desktopLayout && userId == null -> Modifier.widthIn(max = 560.dp).fillMaxSize().align(Alignment.Center)
                        desktop && currentScreen != ScreenTypes.Calendar && currentScreen != ScreenTypes.Calendars &&
                            currentScreen != ScreenTypes.Profile &&
                            currentScreen !is ScreenTypes.EventDetails &&
                            currentScreen !is ScreenTypes.SpaceDetails && currentScreen !is ScreenTypes.EventEditor ->
                            Modifier.widthIn(max = 840.dp).fillMaxSize().align(Alignment.Center)
                        else -> Modifier.fillMaxSize()
                    },
                    backStack = backStack,
                    onBack = onBack,
                    entryDecorators =
                        listOf(
                            rememberSaveableStateHolderNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator(),
                        ),
                    entryProvider =
                        entryProvider {
                            entry<ScreenTypes.Login> {
                                LoginRoot(
                                    onRegister = { backStack.navigateTo(ScreenTypes.Registration) },
                                )
                            }
                            entry<ScreenTypes.Registration> { RegistrationRoot(onBack = onBack) }
                            entry<ScreenTypes.Calendar> {
                                CalendarRoot(
                                    viewModel = checkNotNull(calendar),
                                    onOpenEvent = openEvent,
                                    onCreateEvent = { backStack.navigateTo(ScreenTypes.EventEditor(it)) },
                                    onBoardSettings = { backStack.navigateTo(ScreenTypes.BoardSettings) },
                                    desktopLayout = desktop,
                                    onOpenSpace = openSpace,
                                    onSpaces = { selectTab(ScreenTypes.Calendars) },
                                    onCreateSpace = { backStack.navigateTo(ScreenTypes.CreateSpace) },
                                    onJoinSpace = { backStack.navigateTo(ScreenTypes.JoinSpace) },
                                )
                            }
                            entry<ScreenTypes.Calendars> {
                                SpacesRoot(
                                    onOpenSpace = openSpace,
                                    onCreate = { backStack.navigateTo(ScreenTypes.CreateSpace) },
                                    onJoin = { backStack.navigateTo(ScreenTypes.JoinSpace) },
                                    onInvitations = { backStack.navigateTo(ScreenTypes.Invitations) },
                                    desktopLayout = desktop,
                                )
                            }
                            entry<ScreenTypes.Profile> {
                                ProfileRoot(
                                    onEditProfile = { backStack.navigateTo(ScreenTypes.ProfileEditor) },
                                    onInvitations = { backStack.navigateTo(ScreenTypes.Invitations) },
                                    onAdmin = { backStack.navigateTo(ScreenTypes.Users) },
                                    desktopLayout = desktop,
                                    onNavigationBlockedChanged = { profileNavigationBlocked = it },
                                )
                            }
                            entry<ScreenTypes.ProfileEditor> {
                                ProfileEditorRoot(
                                    onBack = {
                                        refresh()
                                        onBack()
                                    }
                                )
                            }
                            entry<ScreenTypes.BoardSettings> {
                                BoardSettingsRoot(
                                    onBack = onBack,
                                    onSaved = {
                                        refresh()
                                        onBack()
                                    }
                                )
                            }
                            entry<ScreenTypes.CreateSpace> { CreateSpaceRoot(onBack = onBack, onOpenSpace = openSpace) }
                            entry<ScreenTypes.JoinSpace> { JoinSpaceRoot(onBack = onBack, onOpenSpace = openSpace) }
                            entry<ScreenTypes.Invitations> { InvitationsRoot(onBack = onBack, onOpenSpace = openSpace) }
                            entry<ScreenTypes.SpaceDetails> { route ->
                                SpaceDetailsRoot(
                                    spaceId = route.spaceId,
                                    onBack = onBack,
                                    onMembers = { backStack.navigateTo(ScreenTypes.SpaceMembers(route.spaceId)) },
                                    onInvite = { backStack.navigateTo(ScreenTypes.InviteMember(route.spaceId)) },
                                    onSettings = { backStack.navigateTo(ScreenTypes.SpaceSettings(route.spaceId)) },
                                    onCreateEvent = { id ->
                                        backStack.navigateTo(
                                            ScreenTypes.EventEditor(
                                                checkNotNull(calendar)
                                                    .state.value.selectedDate
                                                    .toString(),
                                                spaceId = id,
                                            ),
                                        )
                                    },
                                    onOpenEvent = openEvent,
                                    onLeave = { backStack.navigateTo(ScreenTypes.LeaveSpace(route.spaceId)) },
                                    desktopLayout = desktop,
                                    onSpace = openSpace,
                                    onMember = { backStack.navigateTo(ScreenTypes.MemberRole(route.spaceId, it)) },
                                    onCreateSpace = { backStack.navigateTo(ScreenTypes.CreateSpace) },
                                    onJoinSpace = { backStack.navigateTo(ScreenTypes.JoinSpace) },
                                    onInvitations = { backStack.navigateTo(ScreenTypes.Invitations) },
                                    onBoard = { selectTab(ScreenTypes.Calendar) },
                                    onNavigationBlockedChanged = { spaceNavigationBlocked = it },
                                )
                            }
                            entry<ScreenTypes.SpaceMembers> { route ->
                                SpaceMembersRoot(
                                    spaceId = route.spaceId,
                                    onBack = onBack,
                                    onInvite = { backStack.navigateTo(ScreenTypes.InviteMember(route.spaceId)) },
                                    onMember = { backStack.navigateTo(ScreenTypes.MemberRole(route.spaceId, it)) },
                                )
                            }
                            entry<ScreenTypes.MemberRole> { route ->
                                MemberRoleRoot(
                                    spaceId = route.spaceId,
                                    userId = route.userId,
                                    onBack = onBack,
                                    onLeft = finishSpace,
                                )
                            }
                            entry<ScreenTypes.InviteMember> { route ->
                                InviteMemberRoot(spaceId = route.spaceId, onBack = onBack)
                            }
                            entry<ScreenTypes.SpaceSettings> { route ->
                                SpaceSettingsRoot(
                                    spaceId = route.spaceId,
                                    onBack = onBack,
                                    onLeave = { backStack.navigateTo(ScreenTypes.LeaveSpace(route.spaceId)) },
                                    onDeleted = finishSpace,
                                )
                            }
                            entry<ScreenTypes.LeaveSpace> { route ->
                                LeaveSpaceRoot(
                                    spaceId = route.spaceId,
                                    onBack = onBack,
                                    onLeft = finishSpace,
                                    onSettings = { backStack.navigateTo(ScreenTypes.SpaceSettings(route.spaceId)) },
                                )
                            }
                            entry<ScreenTypes.EventDetails> { route ->
                                val details: @Composable () -> Unit = {
                                    EventDetailsRoot(
                                        eventId = route.eventId,
                                        occurrenceStart = route.occurrenceStart,
                                        onBack = onBack,
                                        onEdit = {
                                            backStack.navigateTo(
                                                ScreenTypes.EventEditor(
                                                    checkNotNull(calendar).state.value.selectedDate.toString(),
                                                    eventId = route.eventId,
                                                ),
                                            )
                                        },
                                        onDeleted = finishEvent,
                                        viewModel = checkNotNull(calendar),
                                        desktopLayout = desktop,
                                    )
                                }
                                if (desktop) {
                                    CalendarRoot(
                                        viewModel = checkNotNull(calendar),
                                        onOpenEvent = { event ->
                                            calendar.rememberEvent(event)
                                            backStack.removeLastOrNull()
                                            backStack.navigateTo(event.detailsRoute())
                                        },
                                        onCreateEvent = { backStack.navigateTo(ScreenTypes.EventEditor(it)) },
                                        onBoardSettings = { backStack.navigateTo(ScreenTypes.BoardSettings) },
                                        desktopLayout = true,
                                        onOpenSpace = openSpace,
                                        onSpaces = { selectTab(ScreenTypes.Calendars) },
                                        onCreateSpace = { backStack.navigateTo(ScreenTypes.CreateSpace) },
                                        onJoinSpace = { backStack.navigateTo(ScreenTypes.JoinSpace) },
                                        detailContent = details,
                                    )
                                } else {
                                    details()
                                }
                            }
                            entry<ScreenTypes.EventEditor> { route ->
                                val eventEditor = checkNotNull(editor)
                                val calendarState by checkNotNull(calendar).state.collectAsStateWithLifecycle()
                                val original = route.eventId?.let { calendar.series(it) }
                                LaunchedEffect(route, original) {
                                    if (route.eventId == null || original != null) {
                                        eventEditor.start(route.date, original, route.spaceId)
                                    }
                                }
                                if (route.eventId != null && original == null &&
                                    !eventEditor.isStarted(route.date, route.eventId, route.spaceId)
                                ) {
                                    EventUnavailableScreen(calendarState.isLoading, calendarState.error, onBack, refresh)
                                } else {
                                    EventEditorRoot(
                                        viewModel = eventEditor,
                                        onBack = onBack,
                                        onVisibility = { backStack.navigateTo(ScreenTypes.EventVisibility) },
                                        onDates = { backStack.navigateTo(ScreenTypes.EventDates) },
                                        onSaved = finishEvent,
                                        desktopLayout = desktop,
                                        onSpace = openSpace,
                                        desktopSidebar = {
                                            val editorState by eventEditor.state.collectAsStateWithLifecycle()
                                            DesktopSpaceSidebar(
                                                calendarState.copy(isSaving = calendarState.isSaving || editorState.isSaving),
                                                { if (!editorState.isSaving && !editorState.isSaved) calendar.onEvent(it) },
                                                openSpace,
                                                { selectTab(ScreenTypes.Calendars) },
                                                {
                                                    if (!editorState.isSaving && !editorState.isSaved) {
                                                        backStack.navigateTo(ScreenTypes.CreateSpace)
                                                    }
                                                },
                                                {
                                                    if (!editorState.isSaving && !editorState.isSaved) {
                                                        backStack.navigateTo(ScreenTypes.JoinSpace)
                                                    }
                                                },
                                            )
                                        },
                                    )
                                }
                            }
                            entry<ScreenTypes.EventVisibility> {
                                EventVisibilityRoot(
                                    viewModel = checkNotNull(editor),
                                    onBack = onBack,
                                    onChooseSpace = { backStack.navigateTo(ScreenTypes.SpaceVisibility(it)) },
                                )
                            }
                            entry<ScreenTypes.SpaceVisibility> { route ->
                                SpaceVisibilityRoot(
                                    spaceId = route.spaceId,
                                    viewModel = checkNotNull(editor),
                                    onBack = onBack,
                                )
                            }
                            entry<ScreenTypes.EventDates> {
                                EventDatesRoot(
                                    viewModel = checkNotNull(editor),
                                    onBack = onBack,
                                )
                            }
                            entry<ScreenTypes.Users> {
                                UsersRoot(onBack = onBack, onEdit = { backStack.navigateTo(ScreenTypes.UserEditor(it)) })
                            }
                            entry<ScreenTypes.UserEditor> { route ->
                                UserEditorRoot(userId = route.userId, onBack = onBack)
                            }
                        },
                )
                if (!desktop && currentScreen is ScreenTypes.BottomBarNavigatable) {
                    AppBottomBar(
                        currentScreen = currentScreen,
                        onSelect = selectTab,
                    )
                }
            }
        }
    }
}

private fun ScreenTypes.isEventRoute(): Boolean =
    this is ScreenTypes.EventDetails || this is ScreenTypes.EventEditor ||
        this is ScreenTypes.EventVisibility || this is ScreenTypes.SpaceVisibility || this is ScreenTypes.EventDates
