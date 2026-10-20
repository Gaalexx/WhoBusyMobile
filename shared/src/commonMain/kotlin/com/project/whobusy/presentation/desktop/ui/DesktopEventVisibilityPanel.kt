package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.ButtonStyle
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyValueButton
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.domain.model.Space
import com.project.whobusy.presentation.event.preview.EventPreviewData
import com.project.whobusy.presentation.event.ui.visibilityTitle
import com.project.whobusy.presentation.event.viewmodel.EditorCommand
import com.project.whobusy.presentation.event.viewmodel.EditorState
import com.project.whobusy.presentation.spaces.ui.spaceColor
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_cancel
import com.project.whobusy.resources.desktop_editor_default_body
import com.project.whobusy.resources.desktop_editor_default_title
import com.project.whobusy.resources.desktop_editor_public_hint
import com.project.whobusy.resources.desktop_editor_visibility_hint
import com.project.whobusy.resources.plan_create
import com.project.whobusy.resources.plan_no_spaces
import com.project.whobusy.resources.plan_save
import com.project.whobusy.resources.plan_visibility
import com.project.whobusy.resources.plan_visible_everyone
import com.project.whobusy.resources.plan_visible_everyone_body
import com.project.whobusy.resources.plan_who_sees
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DesktopEventVisibilityPanel(
    state: EditorState,
    onEvent: (EditorCommand) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val space = state.spaces.firstOrNull { it.id == state.form.spaceId }
    val enabled = !state.isLoading && !state.isSaving && !state.isSaved
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = Color.White) {
        Column {
            LazyColumn(
                Modifier.weight(1f),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                item(key = "title") {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(Res.string.plan_who_sees), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(Res.string.desktop_editor_visibility_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            color = WhoBusyColors.SecondaryText,
                        )
                    }
                }
                if (space != null) {
                    item(key = "public") {
                        WhoBusyNotice(
                            stringResource(Res.string.plan_visible_everyone, space.name),
                            stringResource(Res.string.plan_visible_everyone_body),
                        )
                    }
                } else {
                    if (state.spaces.isEmpty() && !state.isLoading) {
                        item(key = "no-spaces") {
                            Text(stringResource(Res.string.plan_no_spaces), color = WhoBusyColors.SecondaryText)
                        }
                    }
                    items(state.spaces, key = { it.id }) { item ->
                        DesktopEventSpaceVisibility(
                            item,
                            state.form.shares.firstOrNull { it.spaceId == item.id }?.visibility,
                            enabled && state.canChangeVisibility,
                            { onEvent(EditorCommand.Visibility(item.id, it)) },
                        )
                    }
                    item(key = "default") {
                        WhoBusyNotice(
                            stringResource(Res.string.desktop_editor_default_title),
                            stringResource(Res.string.desktop_editor_default_body),
                        )
                    }
                    item(key = "hint") {
                        Text(
                            stringResource(Res.string.desktop_editor_public_hint),
                            color = WhoBusyColors.SecondaryText,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
            (state.form.validationError ?: state.error)?.let {
                Text(
                    stringResource(it),
                    Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    color = MaterialTheme.colorScheme.error,
                )
            }
            Row(
                Modifier.fillMaxWidth().padding(24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                WhoBusyButton(
                    stringResource(if (state.eventId == null) Res.string.plan_create else Res.string.plan_save),
                    { onEvent(EditorCommand.Save) },
                    Modifier.weight(1.6f),
                    enabled = enabled,
                    loading = state.isSaving,
                )
                WhoBusyButton(
                    stringResource(Res.string.action_cancel),
                    onBack,
                    Modifier.weight(1f),
                    style = ButtonStyle.Secondary,
                    enabled = !state.isSaving && !state.isSaved,
                )
            }
        }
    }
}

@Composable
private fun DesktopEventSpaceVisibility(
    space: Space,
    visibility: EventVisibility?,
    enabled: Boolean,
    onVisibility: (EventVisibility?) -> Unit,
) {
    var expanded by remember(space.id) { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            WhoBusyAvatar(space.name, spaceColor(space.id), size = 36.dp)
            Text(space.name)
        }
        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(surface = WhoBusyColors.Background)) {
            Box {
                WhoBusyValueButton(
                    stringResource(Res.string.plan_visibility),
                    visibilityTitle(visibility),
                    { if (enabled) expanded = true },
                    enabled = enabled,
                    trailingIcon = UiIcon.ExpandMore,
                )
                DropdownMenu(expanded, { expanded = false }) {
                    listOf(EventVisibility.Actors, EventVisibility.Busy, null).forEach { value ->
                        DropdownMenuItem(
                            text = { Text(visibilityTitle(value)) },
                            onClick = {
                                onVisibility(value)
                                expanded = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview(widthDp = 384, heightDp = 832)
@Composable
private fun DesktopEventVisibilityPreview() {
    WhoBusyTheme { DesktopEventVisibilityPanel(EventPreviewData.editor, {}, {}) }
}
