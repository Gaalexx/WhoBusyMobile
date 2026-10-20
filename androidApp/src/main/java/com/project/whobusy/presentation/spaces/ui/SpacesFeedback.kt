package com.project.whobusy.presentation.spaces.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
internal fun SpacesFeedback(
    isLoading: Boolean,
    @StringRes error: Int?,
    @StringRes message: Int? = null,
) {
    if (isLoading) LinearProgressIndicator(Modifier.fillMaxWidth())
    error?.let {
        Text(stringResource(it), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.error)
    }
    message?.let {
        Text(stringResource(it), Modifier.padding(16.dp), color = MaterialTheme.colorScheme.primary)
    }
}

@Preview
@Composable
private fun FeedbackPreview() {
    WhoBusyTheme {
        Column {
            SpacesFeedback(true, null)
            SpacesFeedback(false, R.string.spaces_error_action)
        }
    }
}
