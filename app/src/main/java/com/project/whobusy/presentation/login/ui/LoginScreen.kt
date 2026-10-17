package com.project.whobusy.presentation.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyCalendarBadge
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.login.viewmodel.LoginCommand
import com.project.whobusy.presentation.login.viewmodel.LoginState

@Composable
fun LoginScreen(
    state: LoginState,
    onEvent: (LoginCommand) -> Unit,
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Color.White)
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(84.dp))
        WhoBusyCalendarBadge()
        Spacer(Modifier.height(34.dp))
        Text(
            stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            stringResource(R.string.login_subtitle),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(42.dp))
        WhoBusyTextField(
            state.email,
            { onEvent(LoginCommand.EmailChanged(it)) },
            stringResource(R.string.login_email),
            error = state.emailError?.let { stringResource(it) },
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(14.dp))
        WhoBusyTextField(
            state.password,
            { onEvent(LoginCommand.PasswordChanged(it)) },
            stringResource(R.string.login_password),
            error = state.passwordError?.let { stringResource(it) },
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )
        state.error?.let {
            Text(
                stringResource(it),
                Modifier.padding(top = 12.dp),
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(28.dp))
        WhoBusyButton(
            stringResource(R.string.login_submit),
            { onEvent(LoginCommand.Submit) },
            loading = state.isLoading,
        )
        Spacer(Modifier.height(32.dp))
        Text(
            stringResource(R.string.login_account_help),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Preview(name = "Вход", widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreview() {
    WhoBusyTheme { LoginScreen(LoginState("alex@example.com", "password"), {}) }
}

@Preview(name = "Ошибка входа", widthDp = 390, heightDp = 844)
@Composable
private fun LoginErrorPreview() {
    WhoBusyTheme { LoginScreen(LoginState(error = R.string.error_login_credentials), {}) }
}
