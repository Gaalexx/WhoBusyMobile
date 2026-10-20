package com.project.whobusy.core.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ui_profile
import com.project.whobusy.resources.ui_profile_details
import com.project.whobusy.resources.ui_profile_details_body
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusyListRow(
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
) {
    Surface(modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface) {
        Row(
            Modifier
                .fillMaxWidth()
                .heightIn(min = if (leading == null) 72.dp else 76.dp)
                .then(if (onClick == null) Modifier else Modifier.clickable(onClick = onClick))
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) Box(Modifier.size(44.dp), contentAlignment = Alignment.Center) { leading() }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                if (subtitle != null) {
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailing?.invoke()
        }
    }
}

@Preview(widthDp = 390, showBackground = true)
@Composable
private fun ListRowPreview() {
    WhoBusyTheme {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            WhoBusyListRow(
                stringResource(Res.string.ui_profile_details),
                stringResource(Res.string.ui_profile_details_body),
            )
            WhoBusyListRow(
                stringResource(Res.string.ui_profile),
                onClick = {},
                trailing = { WhoBusySelectionMark(true) },
                leading = { WhoBusyAvatar("ГА", MaterialTheme.colorScheme.primary, size = 44.dp) },
            )
        }
    }
}
