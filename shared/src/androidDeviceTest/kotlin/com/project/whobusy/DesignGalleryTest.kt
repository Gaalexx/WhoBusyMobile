package com.project.whobusy

import android.graphics.Bitmap
import android.os.SystemClock
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.EventShare
import com.project.whobusy.domain.model.EventStatus
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.board.ui.BoardSettingsScreen
import com.project.whobusy.presentation.board.viewmodel.BoardSettingsState
import com.project.whobusy.presentation.calendar.model.CalendarMode
import com.project.whobusy.presentation.calendar.ui.CalendarScreen
import com.project.whobusy.presentation.calendar.viewmodel.CalendarState
import com.project.whobusy.presentation.event.preview.EventPreviewData
import com.project.whobusy.presentation.event.ui.EventDatesScreen
import com.project.whobusy.presentation.event.ui.EventDetailsScreen
import com.project.whobusy.presentation.event.ui.EventEditorScreen
import com.project.whobusy.presentation.event.ui.EventVisibilityScreen
import com.project.whobusy.presentation.event.ui.SpaceVisibilityScreen
import com.project.whobusy.presentation.login.ui.LoginScreen
import com.project.whobusy.presentation.login.viewmodel.LoginState
import com.project.whobusy.presentation.navigation.AppBottomBar
import com.project.whobusy.presentation.navigation.ScreenTypes
import com.project.whobusy.presentation.profile.ui.ProfileEditorScreen
import com.project.whobusy.presentation.profile.ui.ProfileScreen
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorState
import com.project.whobusy.presentation.register.ui.RegistrationScreen
import com.project.whobusy.presentation.register.viewmodel.RegistrationState
import com.project.whobusy.presentation.spaces.ui.CreateSpaceScreen
import com.project.whobusy.presentation.spaces.ui.InvitationsScreen
import com.project.whobusy.presentation.spaces.ui.InviteMemberScreen
import com.project.whobusy.presentation.spaces.ui.JoinSpaceScreen
import com.project.whobusy.presentation.spaces.ui.LeaveSpaceScreen
import com.project.whobusy.presentation.spaces.ui.MemberRoleScreen
import com.project.whobusy.presentation.spaces.ui.SpaceDetailsScreen
import com.project.whobusy.presentation.spaces.ui.SpaceMembersScreen
import com.project.whobusy.presentation.spaces.ui.SpaceSettingsScreen
import com.project.whobusy.presentation.spaces.ui.SpacesPreviewData
import com.project.whobusy.presentation.spaces.ui.SpacesScreen
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.auth_profile_edit
import com.project.whobusy.resources.screen_profile
import com.project.whobusy.time.LocalDate
import com.project.whobusy.time.atStartOfDay
import com.project.whobusy.time.atTime
import com.project.whobusy.time.minusDays
import com.project.whobusy.time.of
import com.project.whobusy.time.plusDays
import java.io.File
import org.jetbrains.compose.resources.stringResource
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Runtime render gallery; screenshots are reviewed visually, not compared to golden images. */
@RunWith(AndroidJUnit4::class)
class DesignGalleryTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun galleryAllScreens() {
        val current = mutableIntStateOf(1)
        composeRule.setContent {
            WhoBusyTheme {
                key(current.intValue) { GalleryScreen(current.intValue) }
            }
        }
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val directory =
            File(instrumentation.targetContext.getExternalFilesDir(null), "gallery").apply {
                assertTrue("Gallery directory must be writable", isDirectory || mkdirs())
            }
        galleryNames.forEachIndexed { index, name ->
            composeRule.runOnIdle { current.intValue = index + 1 }
            val tag = "gallery-${index + 1}"
            composeRule.onNodeWithTag(tag).assertIsDisplayed()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.mainClock.advanceTimeByFrame()
            composeRule.waitForIdle()
            // Force every Compose window to draw before SurfaceFlinger captures it.
            // The deletion confirmation uses a separate dialog window.
            val roots = composeRule.onAllNodes(isRoot())
            roots.fetchSemanticsNodes().indices.forEach { roots[it].captureToImage() }
            instrumentation.waitForIdleSync()
            instrumentation.uiAutomation.waitForIdle(100, 1_000)
            SystemClock.sleep(200)
            composeRule.onNodeWithTag(tag).assertIsDisplayed()
            val bitmap = requireNotNull(instrumentation.uiAutomation.takeScreenshot())
            assertTrue("Screen $name must have a phone-sized render", bitmap.width > 300 && bitmap.height > 500)
            val target = File(directory, "${(index + 1).toString().padStart(2, '0')}-$name.png")
            target.outputStream().use { stream ->
                assertTrue(
                    "Screen $name must encode successfully",
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream),
                )
            }
            assertTrue("Screen $name must be saved", target.length() > 0)
        }
    }
}

private val galleryNames =
    listOf(
        "login",
        "register",
        "board-day",
        "board-settings",
        "board-week",
        "board-month",
        "spaces",
        "create-space",
        "join-space",
        "invitations",
        "space-admin",
        "members",
        "member-role",
        "invite-member",
        "space-settings",
        "personal-plan",
        "plan-visibility",
        "visibility-choice",
        "space-plan",
        "plan-details",
        "busy-plan",
        "profile",
        "profile-edit",
        "dates-repeat",
        "last-admin-leave",
        "space-visitor",
        "delete-space",
        "empty-spaces",
    )

@Composable
private fun GalleryScreen(number: Int) {
    val spaces = SpacesPreviewData.state
    val editor = EventPreviewData.editor
    val selected: ScreenTypes.BottomBarNavigatable? =
        when (number) {
            3, 5, 6 -> ScreenTypes.Calendar
            7, 28 -> ScreenTypes.Calendars
            22 -> ScreenTypes.Profile
            else -> null
        }
    Box(Modifier.fillMaxSize().testTag("gallery-$number")) {
        when (number) {
            1 -> LoginScreen(LoginState("gaalex", "password"), {}, {})
            2 ->
                RegistrationScreen(
                    RegistrationState("gaalex", "Александр", "Гайдуков", "alex@example.com", "password"),
                    {},
                    {},
                )

            3 -> GalleryCalendar(CalendarMode.Day)
            4 ->
                BoardSettingsScreen(
                    BoardSettingsState(spaces.spaces, BoardSettings(setOf(1, 2), showOwn = true)),
                    {},
                    {},
                )

            5 -> GalleryCalendar(CalendarMode.Week)
            6 -> GalleryCalendar(CalendarMode.Month)
            7 -> SpacesScreen(spaces, {}, {}, {}, {})
            8 -> CreateSpaceScreen(spaces.copy(name = "Команда разработки"), {}, {})
            9 -> JoinSpaceScreen(spaces.copy(code = "q7Xv_3kM9pLa"), {}, {})
            10 -> InvitationsScreen(spaces, {}, {})
            11 -> GallerySpaceDetails(visitor = false)
            12 -> SpaceMembersScreen(spaces, {}, {}, {})
            13 -> MemberRoleScreen(spaces.copy(memberId = 4, selectedRole = SpaceRole.Actor), {}, {})
            14 -> InviteMemberScreen(spaces.copy(login = "darya", selectedRole = SpaceRole.Actor), {}, {})
            15 -> SpaceSettingsScreen(spaces, {}, {}, {})
            16 ->
                EventEditorScreen(
                    editor.copy(
                        form =
                            editor.form.copy(
                                recurrenceRule = null,
                                shares = editor.spaces.map { EventShare(it.id, EventVisibility.Busy) },
                            ),
                    ),
                    {},
                    {},
                    {},
                    {},
                )

            17 -> EventVisibilityScreen(editor, {}, {})
            18 -> SpaceVisibilityScreen("Momentum", EventVisibility.Actors, {}, {})
            19 ->
                EventEditorScreen(
                    editor.copy(
                        form =
                            editor.form.copy(
                                title = "Обсуждение релиза",
                                start = "2026-10-17T09:00",
                                end = "2026-10-17T10:00",
                                location = "Переговорная № 2",
                                description = "Обсудим готовность новой версии",
                                spaceId = 1,
                                shares = emptyList(),
                            ),
                    ),
                    {},
                    {},
                    {},
                    {},
                )

            20 -> EventDetailsScreen(EventPreviewData.event, true, {}, {}, {}, spaces = EventPreviewData.spaces)
            21 ->
                EventDetailsScreen(
                    GalleryData.busyEvent,
                    false,
                    {},
                    {},
                    {},
                    spaces = EventPreviewData.spaces,
                )

            22 -> GalleryProfile()
            23 ->
                ProfileEditorScreen(
                    ProfileEditorState(
                        userId = 1,
                        login = "gaalex",
                        firstName = "Александр",
                        lastName = "Гайдуков",
                        email = "alex@example.com",
                        currentPassword = "password",
                    ),
                    {},
                    {},
                )

            24 ->
                EventDatesScreen(
                    editor.copy(
                        form =
                            editor.form.copy(
                                allDay = true,
                                start = "2026-10-16T00:00",
                                end = "2026-10-19T00:00",
                                recurrenceRule = null,
                            ),
                    ),
                    {},
                    {},
                )

            25 -> LeaveSpaceScreen(spaces.copy(successorId = 2), {}, {}, {})
            26 -> GallerySpaceDetails(visitor = true)
            27 -> SpaceSettingsScreen(spaces, {}, {}, {}, initialDeleteDialog = true)
            28 -> SpacesScreen(spaces.copy(spaces = emptyList(), invitations = emptyList()), {}, {}, {}, {})
            else -> error("Unknown gallery screen $number")
        }
        if (selected != null) AppBottomBar(selected, {})
    }
}

@Composable
private fun GalleryProfile() {
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(Res.string.screen_profile),
                action = {
                    WhoBusyIconButton(
                        UiIcon.Edit,
                        stringResource(Res.string.auth_profile_edit),
                        {},
                        tint = Color.White,
                    )
                },
            )
        },
    ) {
        ProfileScreen(GalleryData.user, {}, {}, {}, {}, invitationCount = 2)
    }
}

@Composable
private fun GalleryCalendar(mode: CalendarMode) {
    CalendarScreen(GalleryData.calendar.copy(mode = mode), {}, GalleryData.date.atTime(11, 45))
}

@Composable
private fun GallerySpaceDetails(visitor: Boolean) {
    val base = SpacesPreviewData.state
    val state =
        if (visitor) {
            base.copy(
                spaces =
                    base.spaces.map {
                        if (it.id ==
                            1
                        ) {
                            it.copy(myRole = SpaceRole.Visitor, inviteCode = null)
                        } else {
                            it
                        }
                    },
                events = listOf(GalleryData.busyEvent),
                currentUserId = 5,
            )
        } else {
            base
        }
    SpaceDetailsScreen(state, {}, {}, {}, {}, {}, {}, {}, {})
}

private object GalleryData {
    val date: LocalDate = LocalDate.of(2026, 10, 17)
    val user =
        CalendarUser(
            1,
            "alex@example.com",
            "Гайдуков Александр",
            UserRole.USER,
            "#2481CC",
            true,
            login = "gaalex",
            firstName = "Александр",
            lastName = "Гайдуков",
        )
    private val anna = user.copy(id = 2, fullName = "Смирнова Анна", color = "#6D4F9C", login = "anna")
    private val mikhail = user.copy(id = 3, fullName = "Волков Михаил", color = "#357F59", login = "mikhail")
    val busyEvent =
        CalendarEvent(
            3,
            3,
            mikhail.fullName,
            "Занято",
            "",
            "",
            date.atTime(12, 0),
            date.atTime(13, 0),
            false,
            EventStatus.BUSY,
            mikhail.color,
            busyOnly = true,
            canEdit = false,
        )
    val calendar =
        CalendarState(
            selectedDate = date,
            user = user,
            users = listOf(user, anna, mikhail),
            events =
                listOf(
                    EventPreviewData.event.copy(
                        id = 1,
                        title = "Обсуждение релиза",
                        start = date.atTime(9, 0),
                        end = date.atTime(10, 0),
                        spaceId = 1,
                        shares = emptyList(),
                        recurrenceRule = null,
                    ),
                    EventPreviewData.event.copy(
                        id = 2,
                        ownerId = 2,
                        ownerName = anna.fullName,
                        canEdit = false,
                        shares = emptyList(),
                        displayColor = anna.color,
                        recurrenceRule = null,
                    ),
                    busyEvent,
                    EventPreviewData.event.copy(
                        id = 4,
                        title = "Личные задачи",
                        start = date.atTime(14, 0),
                        end = date.atTime(15, 0),
                        shares = listOf(EventShare(1, EventVisibility.Busy)),
                        recurrenceRule = null,
                    ),
                    EventPreviewData.event.copy(
                        id = 5,
                        title = "Отпуск",
                        start = date.minusDays(1).atStartOfDay(),
                        end = date.plusDays(2).atStartOfDay(),
                        allDay = true,
                        ownerId = 2,
                        ownerName = anna.fullName,
                        displayColor = anna.color,
                        shares = emptyList(),
                        recurrenceRule = null,
                    ),
                ),
            visibleOwnerIds = setOf(1, 2, 3),
            accessibleOwnerIds = setOf(2, 3),
            spaces = EventPreviewData.spaces,
            boardSettings = BoardSettings(setOf(1, 2)),
            isLoading = false,
        )
}
