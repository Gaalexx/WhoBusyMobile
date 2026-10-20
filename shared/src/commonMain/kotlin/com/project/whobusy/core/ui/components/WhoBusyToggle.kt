package com.project.whobusy.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ic_switch_thumb
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusyToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier.size(48.dp).toggleable(checked, role = Role.Switch, onValueChange = onCheckedChange),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            Modifier
                .size(38.dp, 22.dp)
                .background(
                    if (checked) MaterialTheme.colorScheme.primary else Color(0xFFBAC2CA),
                    RoundedCornerShape(11.dp),
                ).padding(horizontal = 3.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart,
        ) {
            Image(painterResource(Res.drawable.ic_switch_thumb), null, Modifier.size(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TogglePreview() {
    WhoBusyTheme {
        Row {
            WhoBusyToggle(false, {})
            WhoBusyToggle(true, {})
        }
    }
}
