package com.project.whobusy.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.project.whobusy.core.ui.R
import com.project.whobusy.core.ui.theme.WhoBusyTheme

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
    WhoBusyTheme { WhoBusySectionTitle(stringResource(R.string.ui_plan_color_heading)) }
}
