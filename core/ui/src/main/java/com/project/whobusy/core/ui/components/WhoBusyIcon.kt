package com.project.whobusy.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.R
import com.project.whobusy.core.ui.theme.WhoBusyTheme

enum class UiIcon(
    @param:DrawableRes val resource: Int,
) {
    Calendar(R.drawable.ic_calendar),
    People(R.drawable.ic_people),
    Profile(R.drawable.ic_profile),
    More(R.drawable.ic_more),
    Add(R.drawable.ic_add),
    Back(R.drawable.ic_back),
    Check(R.drawable.ic_check),
    Edit(R.drawable.ic_edit),
}

@Composable
fun WhoBusyIcon(
    icon: UiIcon,
    description: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Icon(painterResource(icon.resource), description, modifier.size(24.dp), tint = tint)
}

@Preview(showBackground = true)
@Composable
private fun IconsPreview() {
    WhoBusyTheme { Row { UiIcon.entries.forEach { WhoBusyIcon(it) } } }
}
