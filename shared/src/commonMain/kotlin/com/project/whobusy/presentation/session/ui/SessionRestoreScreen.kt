package com.project.whobusy.presentation.session.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyLogo
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_retry
import com.project.whobusy.resources.app_name
import com.project.whobusy.resources.auth_restore_error
import com.project.whobusy.resources.auth_restore_loading
import com.project.whobusy.resources.auth_restore_sign_in
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun SessionRestoreScreen(
    loading: Boolean,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier.widthIn(max = 420.dp).fillMaxWidth()
                    .verticalScroll(rememberScrollState()).testTag("session-restore-content"),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                WhoBusyLogo()
                Spacer(Modifier.height(24.dp))
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.headlineMedium)
                Spacer(Modifier.height(16.dp))
                Text(
                    text = stringResource(if (loading) Res.string.auth_restore_loading else Res.string.auth_restore_error),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(28.dp))
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                } else {
                    WhoBusyButton(stringResource(Res.string.action_retry), onRetry)
                    Spacer(Modifier.height(12.dp))
                    WhoBusyButton(
                        text = stringResource(Res.string.auth_restore_sign_in),
                        onClick = onSignIn,
                        style = ButtonStyle.Secondary,
                    )
                }
            }
        }
    }
}

@Preview(name = "Восстановление сессии", widthDp = 390, heightDp = 844)
@Composable
private fun SessionRestoreLoadingPreview() {
    WhoBusyTheme { SessionRestoreScreen(loading = true, onRetry = {}, onSignIn = {}) }
}

@Preview(name = "Ошибка восстановления", widthDp = 390, heightDp = 844)
@Composable
private fun SessionRestoreErrorPreview() {
    WhoBusyTheme { SessionRestoreScreen(loading = false, onRetry = {}, onSignIn = {}) }
}
