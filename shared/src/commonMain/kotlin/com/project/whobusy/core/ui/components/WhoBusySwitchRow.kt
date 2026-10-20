package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ui_all_day
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusySwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Surface(shape = RoundedCornerShape(10.dp)) {
        Row(
            Modifier
                .fillMaxWidth()
                .toggleable(checked, role = Role.Switch, onValueChange = onCheckedChange)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, Modifier.weight(1f))
            Switch(checked, onCheckedChange = null)
        }
    }
}

@Preview
@Composable
private fun SwitchRowPreview() {
    WhoBusyTheme { WhoBusySwitchRow(stringResource(Res.string.ui_all_day), false, {}) }
}
