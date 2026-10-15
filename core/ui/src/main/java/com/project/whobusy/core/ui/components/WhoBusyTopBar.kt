package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    action: @Composable RowScope.() -> Unit = {},
) {
    Row(
        Modifier.fillMaxWidth().background(WhoBusyColors.Header).height(60.dp).padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) WhoBusyIconButton(UiIcon.Back, "Назад", onBack, tint = Color.White)
        else Spacer(Modifier.width(20.dp))
        Text(title, Modifier.weight(1f), color = Color.White, style = MaterialTheme.typography.titleLarge)
        action()
    }
}

@Preview
@Composable
private fun TopBarPreview() {
    WhoBusyTheme { WhoBusyTopBar("Календарь", action = { WhoBusyIconButton(UiIcon.More, "Меню", {}, tint = Color.White) }) }
}
