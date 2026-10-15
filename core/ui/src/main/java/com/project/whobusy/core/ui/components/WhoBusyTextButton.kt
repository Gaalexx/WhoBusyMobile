package com.project.whobusy.core.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyTextButton(
    text: String, onClick: () -> Unit, modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    textStyle: TextStyle = MaterialTheme.typography.labelMedium,
) {
    TextButton(onClick, modifier, colors = ButtonDefaults.textButtonColors(contentColor = color)) {
        Text(text, style = textStyle)
    }
}

@Preview
@Composable
private fun TextButtonPreview() { WhoBusyTheme { WhoBusyTextButton("Сегодня", {}) } }
