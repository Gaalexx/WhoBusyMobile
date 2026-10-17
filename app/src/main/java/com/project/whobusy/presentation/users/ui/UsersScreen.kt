package com.project.whobusy.presentation.users.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
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
                stringResource(R.string.admin_users), onBack,
                action = {
                    if (state.isAdmin) {
                        WhoBusyIconButton(
                            UiIcon.Add,
                            stringResource(R.string.admin_create_user),
                            { onEdit(null) },
                            tint = Color.White,
                        )
                    }
                }
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("users-content")
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            WhoBusyNotice(
                stringResource(R.string.admin_accounts_notice_title),
                stringResource(R.string.admin_accounts_notice_body),
                Modifier.padding(16.dp),
            )
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally).padding(24.dp))
            } else if (!state.isAdmin) {
                Text(stringResource(R.string.admin_error_access), Modifier.padding(20.dp))
            } else {
                state.error?.let {
                    Text(stringResource(it), Modifier.padding(20.dp), color = MaterialTheme.colorScheme.error)
                    WhoBusyButton(
                        stringResource(R.string.action_retry),
                        onRefresh,
                        Modifier.padding(horizontal = 16.dp),
                    )
                }
                if (state.users.isEmpty() && state.error == null) {
                    Text(stringResource(R.string.admin_users_empty), Modifier.padding(20.dp))
                }
                state.users.forEach { user ->
                    WhoBusyListRow(
                        title = user.fullName,
                        subtitle =
                            stringResource(
                                R.string.admin_user_subtitle,
                                user.login,
                                stringResource(
                                    if (user.role ==
                                        UserRole.ADMIN
                                    ) {
                                        R.string.admin_role_admin
                                    } else {
                                        R.string.admin_role_user
                                    },
                                ),
                                stringResource(
                                    if (user.isActive) R.string.admin_status_active else R.string.admin_status_disabled,
                                ),
                            ),
                        onClick = { onEdit(user.id) },
                        leading = { WhoBusyAvatar(user.fullName, calendarColor(user.color), 44.dp) },
                        trailing = { WhoBusyIcon(UiIcon.Edit) },
                    )
                }
                Spacer(Modifier.height(24.dp))
                WhoBusyButton(
                    stringResource(R.string.admin_create_user),
                    { onEdit(null) },
                    Modifier.padding(horizontal = 16.dp),
                )
            }
            Spacer(Modifier.height(24.dp))
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
        UsersScreen(UsersState(isLoading = false, isAdmin = true, error = R.string.admin_error_load), {}, {}, {})
    }
}
