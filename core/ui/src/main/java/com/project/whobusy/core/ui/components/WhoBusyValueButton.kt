package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.R
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyValueButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick,
        modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(label, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview
@Composable
private fun ValueButtonPreview() {
    WhoBusyTheme { WhoBusyValueButton(stringResource(R.string.ui_start), "15 октября 2026, 14:00", {}) }
}
