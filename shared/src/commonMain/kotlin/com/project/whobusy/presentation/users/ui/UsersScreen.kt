package com.project.whobusy.presentation.users.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.core.ui.theme.calendarColor
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.calendar.preview.CalendarPreviewData
import com.project.whobusy.presentation.users.viewmodel.UsersState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_retry
import com.project.whobusy.resources.admin_accounts_notice_body
import com.project.whobusy.resources.admin_accounts_notice_title
import com.project.whobusy.resources.admin_create_user
import com.project.whobusy.resources.admin_error_access
import com.project.whobusy.resources.admin_error_load
import com.project.whobusy.resources.admin_role_admin
import com.project.whobusy.resources.admin_role_user
import com.project.whobusy.resources.admin_status_active
import com.project.whobusy.resources.admin_status_disabled
import com.project.whobusy.resources.admin_user_subtitle
import com.project.whobusy.resources.admin_users
import com.project.whobusy.resources.admin_users_empty
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun UsersScreen(
    state: UsersState,
    onBack: () -> Unit,
    onEdit: (Int?) -> Unit,
    onRefresh: () -> Unit,
) {
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(Res.string.admin_users), onBack,
                action = {
                    if (state.isAdmin) {
                        WhoBusyIconButton(
                            UiIcon.Add,
                            stringResource(Res.string.admin_create_user),
                            { onEdit(null) },
                            tint = Color.White,
                        )
                    }
                }
            )
        }
    ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .testTag("users-content")
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            item(key = "notice", contentType = "notice") {
                WhoBusyNotice(
                    stringResource(Res.string.admin_accounts_notice_title),
                    stringResource(Res.string.admin_accounts_notice_body),
                    Modifier.padding(16.dp),
                )
            }
            if (state.isLoading) {
                item(key = "loading", contentType = "loading") {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (!state.isAdmin) {
                item(key = "access-error", contentType = "message") {
                    Text(stringResource(Res.string.admin_error_access), Modifier.padding(20.dp))
                }
            } else {
                state.error?.let { error ->
                    item(key = "load-error", contentType = "message") {
                        Text(stringResource(error), Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error)
                    }
                    item(key = "retry", contentType = "button") {
                        WhoBusyButton(
                            stringResource(Res.string.action_retry),
                            onRefresh,
                            Modifier.padding(horizontal = 16.dp),
                        )
                    }
                }
                if (state.users.isEmpty() && state.error == null) {
                    item(key = "empty", contentType = "message") {
                        Text(stringResource(Res.string.admin_users_empty), Modifier.padding(20.dp))
                    }
                }
                items(state.users, key = { it.id }, contentType = { "user" }) { user ->
                    WhoBusyListRow(
                        title = user.fullName,
                        subtitle =
                            stringResource(
                                Res.string.admin_user_subtitle,
                                user.login,
                                stringResource(
                                    if (user.role ==
                                        UserRole.ADMIN
                                    ) {
                                        Res.string.admin_role_admin
                                    } else {
                                        Res.string.admin_role_user
                                    },
                                ),
                                stringResource(
                                    if (user.isActive) Res.string.admin_status_active else Res.string.admin_status_disabled,
                                ),
                            ),
                        onClick = { onEdit(user.id) },
                        leading = { WhoBusyAvatar(user.fullName, calendarColor(user.color), 44.dp) },
                        trailing = { WhoBusyIcon(UiIcon.Edit) },
                    )
                }
                item(key = "create-spacing", contentType = "spacing") { Spacer(Modifier.height(24.dp)) }
                item(key = "create-user", contentType = "button") {
                    WhoBusyButton(
                        stringResource(Res.string.admin_create_user),
                        { onEdit(null) },
                        Modifier.padding(horizontal = 16.dp),
                    )
                }
            }
            item(key = "bottom-spacing", contentType = "spacing") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun UsersPreview() {
    WhoBusyTheme {
        UsersScreen(UsersState(users = CalendarPreviewData.state.users, isLoading = false, isAdmin = true), {}, {}, {})
    }
}

@Preview(name = "Ошибка загрузки пользователей", widthDp = 390, heightDp = 844)
@Composable
private fun UsersErrorPreview() {
    WhoBusyTheme {
        UsersScreen(UsersState(isLoading = false, isAdmin = true, error = Res.string.admin_error_load), {}, {}, {})
    }
}
