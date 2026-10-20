package com.project.whobusy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ui_action_back
import com.project.whobusy.resources.ui_action_menu
import com.project.whobusy.resources.ui_calendar
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusyTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    action: @Composable RowScope.() -> Unit = {},
    subtitle: String? = null,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(WhoBusyColors.Header)
            .height(68.dp)
            .padding(end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (onBack != null) {
            WhoBusyIconButton(UiIcon.Back, stringResource(Res.string.ui_action_back), onBack, tint = Color.White)
        } else {
            Spacer(Modifier.width(20.dp))
        }
        Column(Modifier.weight(1f)) {
            Text(title, color = Color.White, style = MaterialTheme.typography.titleLarge)
            subtitle?.let {
                Text(it, color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }
        action()
    }
}

@Preview
@Composable
private fun TopBarPreview() {
    WhoBusyTheme {
        WhoBusyTopBar(
            stringResource(Res.string.ui_calendar),
            action = {
                WhoBusyIconButton(UiIcon.More, stringResource(Res.string.ui_action_menu), {}, tint = Color.White)
            }
        )
    }
}
