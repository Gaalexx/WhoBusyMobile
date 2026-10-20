package com.project.whobusy.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.ui_plan_color_heading
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun WhoBusySectionTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        modifier,
        color = MaterialTheme.colorScheme.primary,
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Medium,
    )
}

@Preview(showBackground = true)
@Composable
private fun SectionTitlePreview() {
    WhoBusyTheme { WhoBusySectionTitle(stringResource(Res.string.ui_plan_color_heading)) }
}
