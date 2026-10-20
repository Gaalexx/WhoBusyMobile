package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyEventCard
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.CalendarEvent
import com.project.whobusy.presentation.calendar.model.calendarLocale
import com.project.whobusy.presentation.calendar.ui.eventColor
import com.project.whobusy.presentation.calendar.ui.eventTime
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.plan_busy_notice
import com.project.whobusy.resources.plan_visibility_actors
import com.project.whobusy.resources.plan_visible_everyone
import com.project.whobusy.resources.spaces_access_body
import com.project.whobusy.resources.spaces_access_title
import com.project.whobusy.resources.spaces_add_plan
import com.project.whobusy.resources.spaces_event_subtitle
import com.project.whobusy.resources.spaces_invite
import com.project.whobusy.resources.spaces_invite_hint
import com.project.whobusy.resources.spaces_leave
import com.project.whobusy.resources.spaces_member_role
import com.project.whobusy.resources.spaces_members
import com.project.whobusy.resources.spaces_members_manage
import com.project.whobusy.resources.spaces_members_view
import com.project.whobusy.resources.spaces_no_plans
import com.project.whobusy.resources.spaces_open_plans
import com.project.whobusy.resources.spaces_settings
import com.project.whobusy.resources.spaces_settings_hint
import com.project.whobusy.resources.spaces_title
import com.project.whobusy.resources.spaces_today
import com.project.whobusy.resources.spaces_visibility_body
import com.project.whobusy.resources.spaces_visibility_title
import com.project.whobusy.time.DateTimeFormatter
import com.project.whobusy.time.format
import com.project.whobusy.time.uppercase
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SpaceDetailsScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onBack: () -> Unit,
    onMembers: () -> Unit,
    onInvite: () -> Unit,
    onSettings: () -> Unit,
    onCreateEvent: (Int) -> Unit,
    onOpenEvent: (CalendarEvent) -> Unit,
    onLeave: () -> Unit,
) {
    val space = state.space
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                space?.name ?: stringResource(Res.string.spaces_title),
                onBack,
                action = {
                    if (space?.myRole?.canManage == true) {
                        WhoBusyIconButton(
                            UiIcon.More,
                            stringResource(Res.string.spaces_settings),
                            onSettings,
                            tint = Color.White,
                        )
                    }
                },
                subtitle =
                    space
                        ?.takeUnless {
                            it.myRole.canManage
                        }?.let {
                            stringResource(
                                Res.string.spaces_member_role,
                                it.memberCount,
                                spaceRoleTitle(it.myRole)
                            )
                        },
            )
        }
    ) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 32.dp)) {
            item(key = "feedback") {
                SpacesFeedback(state.isLoading, state.error)
            }
            if (space != null) {
                if (space.myRole.canManage && !state.showPlans) {
                    item(key = "summary") {
                        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Spacer(Modifier.height(24.dp))
                            WhoBusyAvatar(space.name.take(1), spaceColor(space.id), size = 80.dp)
                            Spacer(Modifier.height(20.dp))
                            Text(space.name, style = MaterialTheme.typography.titleLarge)
                            Text(
                                stringResource(
                                    Res.string.spaces_member_role,
                                    space.memberCount,
                                    spaceRoleTitle(space.myRole),
                                ),
                                Modifier.padding(top = 8.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            Spacer(Modifier.height(32.dp))
                            WhoBusyButton(
                                stringResource(Res.string.spaces_open_plans),
                                {
                                    onEvent(SpacesCommand.ShowPlans)
                                },
                                Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                } else {
                    item(key = "plans-header") {
                        Column {
                            WhoBusyNotice(
                                stringResource(Res.string.spaces_access_title),
                                stringResource(Res.string.spaces_access_body),
                                Modifier.padding(16.dp),
                            )
                            val day = state.today
                            WhoBusySectionTitle(
                                stringResource(
                                    Res.string.spaces_today,
                                    day.format(DateTimeFormatter.ofPattern("d MMMM", calendarLocale))
                                        .uppercase(calendarLocale),
                                ),
                                Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                            )
                            if (state.events.isEmpty()) {
                                Text(
                                    stringResource(Res.string.spaces_no_plans),
                                    Modifier.padding(20.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                    items(
                        state.events,
                        key = { "event:${it.id}:${it.start}" },
                        contentType = { "event" },
                    ) { event ->
                        val visibility =
                            when {
                                event.busyOnly -> stringResource(Res.string.plan_busy_notice)
                                event.spaceId != null -> stringResource(Res.string.plan_visible_everyone, space.name)
                                else -> stringResource(Res.string.plan_visibility_actors)
                            }
                        WhoBusyEventCard(
                            event.title,
                            stringResource(
                                Res.string.spaces_event_subtitle,
                                eventTime(event),
                                event.ownerName,
                                visibility,
                            ),
                            eventColor(event),
                            {
                                onOpenEvent(event)
                            },
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth().height(82.dp),
                        )
                    }
                    item(key = "create-plan") {
                        Column {
                            Spacer(Modifier.height(40.dp))
                            WhoBusyButton(
                                stringResource(Res.string.spaces_add_plan),
                                {
                                    onCreateEvent(space.id)
                                },
                                Modifier.padding(horizontal = 16.dp)
                            )
                            Spacer(Modifier.height(32.dp))
                        }
                    }
                }
                item(key = "members") {
                    WhoBusyListRow(
                        stringResource(Res.string.spaces_members),
                        stringResource(
                            if (space.myRole.canManage) Res.string.spaces_members_manage else Res.string.spaces_members_view,
                            space.memberCount,
                        ),
                        onMembers,
                    )
                }
                if (space.myRole.canManage) {
                    item(key = "management") {
                        Column {
                            WhoBusyListRow(
                                stringResource(Res.string.spaces_invite),
                                stringResource(Res.string.spaces_invite_hint),
                                onInvite,
                            )
                            WhoBusyListRow(
                                stringResource(Res.string.spaces_settings),
                                stringResource(Res.string.spaces_settings_hint),
                                onSettings,
                            )
                            WhoBusyNotice(
                                stringResource(Res.string.spaces_visibility_title),
                                stringResource(Res.string.spaces_visibility_body),
                                Modifier.padding(horizontal = 16.dp, vertical = 40.dp),
                            )
                        }
                    }
                } else {
                    item(key = "leave") {
                        Column {
                            Spacer(Modifier.height(64.dp))
                            WhoBusyButton(
                                stringResource(Res.string.spaces_leave),
                                onLeave,
                                Modifier.padding(horizontal = 16.dp),
                                style = ButtonStyle.Destructive,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AdminSpacePreview() {
    WhoBusyTheme { SpaceDetailsScreen(SpacesPreviewData.state, {}, {}, {}, {}, {}, {}, {}, {}) }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun VisitorSpacePreview() {
    WhoBusyTheme { SpaceDetailsScreen(SpacesPreviewData.state.copy(spaceId = 3), {}, {}, {}, {}, {}, {}, {}, {}) }
}
