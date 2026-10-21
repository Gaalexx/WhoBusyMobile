package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ui_profile_details
import com.project.whobusy.resources.ui_profile_details_body
import org.jetbrains.compose.resources.stringResource

@Composable
fun WhoBusySectionCard(
    title: String,
    icon: UiIcon,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                WhoBusyIcon(icon, tint = MaterialTheme.colorScheme.primary)
                Text(title, style = MaterialTheme.typography.titleMedium)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            content()
        }
    }
}

@Preview(widthDp = 390, showBackground = true)
@Composable
private fun SectionCardPreview() {
    WhoBusyTheme {
        WhoBusySectionCard(stringResource(Res.string.ui_profile_details), UiIcon.Profile) {
            Text(stringResource(Res.string.ui_profile_details_body))
        }
    }
}
