package com.project.whobusy.presentation.profile.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.components.WhoBusyTextButton
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_cancel
import com.project.whobusy.resources.action_logout
import com.project.whobusy.resources.auth_logout_all_confirmation
import com.project.whobusy.resources.auth_logout_all_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ProfileLogoutAllDialog(
    isLoggingOut: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isLoggingOut) onCancel() },
        title = { Text(stringResource(Res.string.auth_logout_all_confirmation)) },
        text = { Text(stringResource(Res.string.auth_logout_all_description)) },
        confirmButton = {
            WhoBusyTextButton(
                text = stringResource(Res.string.action_logout),
                onClick = onConfirm,
                enabled = !isLoggingOut,
                loading = isLoggingOut,
            )
        },
        dismissButton = {
            WhoBusyTextButton(
                text = stringResource(Res.string.action_cancel),
                onClick = onCancel,
                enabled = !isLoggingOut,
            )
        },
    )
}

@Preview(name = "Подтверждение выхода", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileLogoutAllDialogPreview() {
    WhoBusyTheme {
        ProfileLogoutAllDialog(isLoggingOut = false, onCancel = {}, onConfirm = {})
    }
}

@Preview(name = "Выполняется выход", widthDp = 390, heightDp = 844)
@Composable
private fun ProfileLogoutAllDialogLoadingPreview() {
    WhoBusyTheme {
        ProfileLogoutAllDialog(isLoggingOut = true, onCancel = {}, onConfirm = {})
    }
}
