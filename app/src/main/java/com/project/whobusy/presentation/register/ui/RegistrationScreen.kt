package com.project.whobusy.presentation.register.ui

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.register.viewmodel.RegistrationCommand
import com.project.whobusy.presentation.register.viewmodel.RegistrationState

@Composable
fun RegistrationScreen(
    state: RegistrationState,
    onEvent: (RegistrationCommand) -> Unit,
    onBack: () -> Unit,
) {
    BackHandler(enabled = state.isLoading) {}
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(stringResource(R.string.auth_create_account), onBack = { if (!state.isLoading) onBack() })
        },
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .testTag("registration-content")
                .imePadding()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 24.dp),
        ) {
            WhoBusyTextField(
                state.login,
                { onEvent(RegistrationCommand.LoginChanged(it)) },
                stringResource(R.string.auth_login),
                Modifier.heightIn(min = 64.dp),
                error = state.loginError?.let { stringResource(it) },
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Text(
                stringResource(R.string.auth_login_rules),
                Modifier.padding(start = 4.dp, top = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(22.dp))
            WhoBusyTextField(
                state.lastName,
                { onEvent(RegistrationCommand.LastNameChanged(it)) },
                stringResource(R.string.auth_last_name),
                Modifier.heightIn(min = 64.dp),
                error = state.lastNameError?.let { stringResource(it) },
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(12.dp))
            WhoBusyTextField(
                state.firstName,
                { onEvent(RegistrationCommand.FirstNameChanged(it)) },
                stringResource(R.string.auth_first_name),
                Modifier.heightIn(min = 64.dp),
                error = state.firstNameError?.let { stringResource(it) },
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(12.dp))
            WhoBusyTextField(
                state.email,
                { onEvent(RegistrationCommand.EmailChanged(it)) },
                stringResource(R.string.auth_optional_email),
                Modifier.heightIn(min = 64.dp),
                error = state.emailError?.let { stringResource(it) },
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            )
            Spacer(Modifier.height(12.dp))
            WhoBusyTextField(
                state.password,
                { onEvent(RegistrationCommand.PasswordChanged(it)) },
                stringResource(R.string.login_password),
                Modifier.heightIn(min = 64.dp),
                error = state.passwordError?.let { stringResource(it) },
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                visualTransformation = PasswordVisualTransformation(),
            )
            Spacer(Modifier.height(26.dp))
            WhoBusyNotice(
                stringResource(R.string.auth_registration_notice_title),
                stringResource(R.string.auth_registration_notice_body),
            )
            state.error?.let {
                Text(stringResource(it), Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error)
            }
            Spacer(Modifier.height(90.dp))
            WhoBusyButton(
                stringResource(R.string.auth_register),
                { onEvent(RegistrationCommand.Submit) },
                loading = state.isLoading,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun RegistrationPreview() {
    WhoBusyTheme {
        RegistrationScreen(
            RegistrationState("gaalex", "Александр", "Гайдуков", "alex@example.com", "password"),
            {},
            {},
        )
    }
}

@Preview(name = "Ошибки регистрации", widthDp = 390, heightDp = 844)
@Composable
private fun RegistrationErrorPreview() {
    WhoBusyTheme {
        RegistrationScreen(
            RegistrationState(login = "alex", loginError = R.string.auth_error_login_taken),
            {},
            {},
        )
    }
}
