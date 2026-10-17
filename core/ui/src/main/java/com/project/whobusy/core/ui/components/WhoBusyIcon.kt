package com.project.whobusy.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme

enum class UiIcon(
    val imageVector: ImageVector,
) {
    Calendar(Icons.Filled.CalendarMonth),
    CalendarBrand(Icons.Filled.CalendarMonth),
    People(Icons.Filled.People),
    Profile(Icons.Filled.Person),
    More(Icons.Filled.MoreVert),
    Add(Icons.Filled.Add),
    Back(Icons.AutoMirrored.Filled.ArrowBack),
    Check(Icons.Filled.Check),
    Edit(Icons.Filled.Edit),
    Filter(Icons.Filled.FilterList),
    Key(Icons.Filled.Key),
    Mail(Icons.Filled.Mail),
    Lock(Icons.Filled.Lock),
}

@Composable
fun WhoBusyIcon(
    icon: UiIcon,
    description: String? = null,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Icon(icon.imageVector, description, modifier.size(24.dp), tint = tint)
}

@Preview(showBackground = true)
@Composable
private fun IconsPreview() {
    WhoBusyTheme { Row { UiIcon.entries.forEach { WhoBusyIcon(it) } } }
}
