package com.project.whobusy.presentation.profile.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyColorPalette
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.platform.PlatformBackHandler
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorCommand
import com.project.whobusy.presentation.profile.viewmodel.ProfileEditorState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.auth_error_current_password
import com.project.whobusy.resources.auth_first_name
import com.project.whobusy.resources.auth_last_name
import com.project.whobusy.resources.auth_optional_email
import com.project.whobusy.resources.auth_profile_color_heading
import com.project.whobusy.resources.auth_profile_current_password
import com.project.whobusy.resources.auth_profile_details
import com.project.whobusy.resources.auth_profile_login_notice
import com.project.whobusy.resources.auth_profile_login_readonly
import com.project.whobusy.resources.auth_profile_new_password
import com.project.whobusy.resources.auth_profile_password_unchanged
import com.project.whobusy.resources.auth_profile_save
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ProfileEditorScreen(
    state: ProfileEditorState,
    onEvent: (ProfileEditorCommand) -> Unit,
    onBack: () -> Unit,
) {
    PlatformBackHandler(enabled = state.isSaving) {}
    val onSave = { if (!state.isSaving) onEvent(ProfileEditorCommand.Save) }
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(
                stringResource(Res.string.auth_profile_details),
                onBack = { if (!state.isSaving) onBack() },
                action = {
                    WhoBusyIconButton(
                        UiIcon.Check,
                        stringResource(Res.string.auth_profile_save),
                        onSave,
                        tint = Color.White,
                    )
                },
            )
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("profile-editor-content")
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(16.dp),
        ) {
            WhoBusyNotice(
                stringResource(Res.string.auth_profile_login_notice, state.login),
                stringResource(Res.string.auth_profile_login_readonly),
            )
            Spacer(Modifier.height(34.dp))
            WhoBusyTextField(
                state.lastName,
                { onEvent(ProfileEditorCommand.LastNameChanged(it)) },
                stringResource(Res.string.auth_last_name),
                Modifier.heightIn(min = 64.dp),
                error = state.lastNameError?.let { stringResource(it) },
                enabled = !state.isSaving,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(10.dp))
            WhoBusyTextField(
                state.firstName,
                { onEvent(ProfileEditorCommand.FirstNameChanged(it)) },
                stringResource(Res.string.auth_first_name),
                Modifier.heightIn(min = 64.dp),
                error = state.firstNameError?.let { stringResource(it) },
                enabled = !state.isSaving,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(10.dp))
            WhoBusyTextField(
                state.email,
                { onEvent(ProfileEditorCommand.EmailChanged(it)) },
                stringResource(Res.string.auth_optional_email),
                Modifier.heightIn(min = 64.dp),
                error = state.emailError?.let { stringResource(it) },
                enabled = !state.isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(28.dp))
            WhoBusySectionTitle(
                stringResource(Res.string.auth_profile_color_heading),
                Modifier.padding(horizontal = 4.dp),
            )
            Spacer(Modifier.height(7.dp))
            WhoBusyColorPalette(state.color, { onEvent(ProfileEditorCommand.ColorChanged(it)) })
            Spacer(Modifier.height(15.dp))
            WhoBusyTextField(
                state.currentPassword,
                { onEvent(ProfileEditorCommand.CurrentPasswordChanged(it)) },
                stringResource(Res.string.auth_profile_current_password),
                Modifier.heightIn(min = 64.dp),
                error = state.currentPasswordError?.let { stringResource(it) },
                enabled = !state.isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(Modifier.height(10.dp))
            WhoBusyTextField(
                state.newPassword,
                { onEvent(ProfileEditorCommand.NewPasswordChanged(it)) },
                stringResource(Res.string.auth_profile_new_password),
                Modifier.heightIn(min = 64.dp),
                error = state.passwordError?.let { stringResource(it) },
                enabled = !state.isSaving,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation(),
                placeholder = stringResource(Res.string.auth_profile_password_unchanged),
            )
            state.error?.let {
                Text(stringResource(it), Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(60.dp))
            WhoBusyButton(
                stringResource(Res.string.auth_profile_save),
                onSave,
                loading = state.isSaving,
                enabled = state.userId != null,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditorPreview() {
    WhoBusyTheme {
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
    }
}

@Preview(name = "Ошибка смены пароля", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileEditorErrorPreview() {
    WhoBusyTheme {
        ProfileEditorScreen(
            ProfileEditorState(
                userId = 1,
                login = "demo",
                firstName = "Александр",
                lastName = "Гайдуков",
                newPassword = "password",
                currentPasswordError = Res.string.auth_error_current_password,
            ),
            {},
            {},
        )
    }
}
