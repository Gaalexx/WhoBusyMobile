package com.project.whobusy.presentation.users.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyColorPalette
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusySwitchRow
import com.project.whobusy.core.ui.components.WhoBusyTabs
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.UserRole
import com.project.whobusy.presentation.users.viewmodel.UserEditorCommand
import com.project.whobusy.presentation.users.viewmodel.UserEditorState

@Composable
fun UserEditorScreen(
    state: UserEditorState,
    onEvent: (UserEditorCommand) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(enabled = state.isSaving) {}
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(if (state.userId == null) R.string.admin_create_user else R.string.admin_edit_user),
                onBack = { if (!state.isSaving) onBack() },
            )
        }
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("user-editor-content")
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(Modifier.align(Alignment.CenterHorizontally).padding(24.dp))
            } else if (!state.isAdmin) {
                Text(stringResource(R.string.admin_error_access))
            } else {
                WhoBusyTextField(
                    state.login,
                    {
                        onEvent(UserEditorCommand.LoginChanged(it))
                    },
                    stringResource(R.string.auth_login), enabled = !state.isSaving
                )
                WhoBusyTextField(
                    state.lastName,
                    {
                        onEvent(UserEditorCommand.LastNameChanged(it))
                    },
                    stringResource(R.string.auth_last_name), enabled = !state.isSaving
                )
                WhoBusyTextField(
                    state.firstName,
                    {
                        onEvent(UserEditorCommand.FirstNameChanged(it))
                    },
                    stringResource(R.string.auth_first_name), enabled = !state.isSaving
                )
                WhoBusyTextField(
                    state.email,
                    { onEvent(UserEditorCommand.EmailChanged(it)) },
                    stringResource(R.string.auth_optional_email),
                    enabled = !state.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                WhoBusyTextField(
                    state.password,
                    { onEvent(UserEditorCommand.PasswordChanged(it)) },
                    stringResource(if (state.userId == null) R.string.login_password else R.string.admin_new_password),
                    enabled = !state.isSaving,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                )
                WhoBusySectionTitle(stringResource(R.string.admin_global_role))
                WhoBusyTabs(
                    listOf(stringResource(R.string.admin_role_user), stringResource(R.string.admin_role_admin)),
                    if (state.role == UserRole.ADMIN) 1 else 0,
                    { onEvent(UserEditorCommand.RoleChanged(if (it == 1) UserRole.ADMIN else UserRole.USER)) },
                )
                WhoBusySectionTitle(stringResource(R.string.admin_color))
                WhoBusyColorPalette(state.color, { onEvent(UserEditorCommand.ColorChanged(it)) })
                WhoBusySwitchRow(stringResource(R.string.admin_account_active), state.isActive) {
                    onEvent(UserEditorCommand.ActiveChanged(it))
                }
                state.error?.let {
                    Text(stringResource(it), color = MaterialTheme.colorScheme.error)
                }
                Spacer(Modifier.height(12.dp))
                WhoBusyButton(
                    stringResource(if (state.userId == null) R.string.admin_create_user else R.string.admin_save_user),
                    { onEvent(UserEditorCommand.Save) },
                    loading = state.isSaving,
                )
                if (state.canDelete) {
                    WhoBusyButton(
                        stringResource(R.string.admin_delete_user),
                        { onEvent(UserEditorCommand.RequestDelete) },
                        style = ButtonStyle.Destructive,
                        enabled = !state.isSaving,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { onEvent(UserEditorCommand.CancelDelete) },
            title = { Text(stringResource(R.string.admin_delete_confirmation)) },
            text = { Text(stringResource(R.string.admin_delete_warning)) },
            confirmButton = {
                WhoBusyTextButton(stringResource(R.string.action_delete), { onEvent(UserEditorCommand.ConfirmDelete) })
            },
            dismissButton = {
                WhoBusyTextButton(stringResource(R.string.action_cancel), { onEvent(UserEditorCommand.CancelDelete) })
            },
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun NewUserPreview() {
    WhoBusyTheme { UserEditorScreen(UserEditorState(isAdmin = true, initialized = true, isLoading = false), {}, {}) }
}

@Preview(name = "Редактирование пользователя", widthDp = 390, heightDp = 844)
@Composable
private fun EditUserPreview() {
    WhoBusyTheme {
        UserEditorScreen(
            UserEditorState(
                userId = 2,
                actingUserId = 100,
                isAdmin = true,
                initialized = true,
                isLoading = false,
                login = "anna",
                firstName = "Анна",
                lastName = "Смирнова",
                email = "anna@example.test",
            ),
            {},
            {},
        )
    }
}

@Preview(name = "Подтверждение удаления", widthDp = 390, heightDp = 844)
@Composable
private fun DeleteUserPreview() {
    WhoBusyTheme {
        UserEditorScreen(
            UserEditorState(
                userId = 2,
                actingUserId = 100,
                isAdmin = true,
                initialized = true,
                isLoading = false,
                login = "anna",
                firstName = "Анна",
                showDeleteConfirmation = true,
            ),
            {},
            {},
        )
    }
}
