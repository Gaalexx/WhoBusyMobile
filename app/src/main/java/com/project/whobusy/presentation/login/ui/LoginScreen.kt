package com.project.whobusy.presentation.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.*
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.login.viewmodel.*

@Composable
fun LoginScreen(state: LoginState, onEvent: (LoginCommand) -> Unit) {
    Column(
        Modifier.fillMaxSize().background(Color.White).safeDrawingPadding().imePadding()
            .verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(84.dp))
        WhoBusyCalendarBadge()
        Spacer(Modifier.height(34.dp))
        Text("Календарь занятости", style = MaterialTheme.typography.headlineMedium, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text("Планы команды в одном месте", color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        Spacer(Modifier.height(42.dp))
        WhoBusyTextField(
            state.email, { onEvent(LoginCommand.EmailChanged(it)) }, "Email",
            error = state.emailError, enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
        )
        Spacer(Modifier.height(14.dp))
        WhoBusyTextField(
            state.password, { onEvent(LoginCommand.PasswordChanged(it)) }, "Пароль",
            error = state.passwordError, enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            visualTransformation = PasswordVisualTransformation(),
        )
        state.error?.let { Text(it, Modifier.padding(top = 12.dp), color = MaterialTheme.colorScheme.error) }
        Spacer(Modifier.height(28.dp))
        WhoBusyButton("Войти", { onEvent(LoginCommand.Submit) }, loading = state.isLoading)
        Spacer(Modifier.height(32.dp))
        Text(
            "Нет аккаунта или забыли пароль?\nОбратитесь к администратору.",
            style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(32.dp))
    }
}

@Preview(name = "Вход", widthDp = 390, heightDp = 844)
@Composable
private fun LoginPreview() { WhoBusyTheme { LoginScreen(LoginState("alex@example.com", "password"), {}) } }

@Preview(name = "Ошибка входа", widthDp = 390, heightDp = 844)
@Composable
private fun LoginErrorPreview() {
    WhoBusyTheme { LoginScreen(LoginState(error = "Неверный email или пароль"), {}) }
}
