package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ui_action_continue
import com.project.whobusy.resources.ui_action_today
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusyTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
    enabled: Boolean = true,
    loading: Boolean = false,
) {
    TextButton(
        onClick,
        modifier,
        enabled = enabled && !loading,
        colors = ButtonDefaults.textButtonColors(contentColor = color)
    ) {
        if (loading) {
            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = textStyle)
    }
}

@Preview
@Composable
private fun TextButtonPreview() {
    WhoBusyTheme { WhoBusyTextButton(stringResource(Res.string.ui_action_today), {}) }
}

@Preview(name = "Недоступна")
@Composable
private fun DisabledTextButtonPreview() {
    WhoBusyTheme { WhoBusyTextButton(stringResource(Res.string.ui_action_continue), {}, enabled = false) }
}

@Preview(name = "Загрузка")
@Composable
private fun LoadingTextButtonPreview() {
    WhoBusyTheme { WhoBusyTextButton(stringResource(Res.string.ui_action_continue), {}, loading = true) }
}
