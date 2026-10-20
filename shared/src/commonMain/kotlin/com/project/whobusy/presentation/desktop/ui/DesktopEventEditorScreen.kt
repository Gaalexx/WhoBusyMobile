package com.project.whobusy.presentation.desktop.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyIconButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.event.preview.EventPreviewData
import com.project.whobusy.presentation.event.viewmodel.EditorCommand
import com.project.whobusy.presentation.event.viewmodel.EditorState
import com.project.whobusy.presentation.spaces.ui.spaceColor
import com.project.whobusy.presentation.spaces.ui.spaceRoleTitle
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.action_cancel
import com.project.whobusy.resources.board_my_plans
import com.project.whobusy.resources.desktop_editor_subtitle
import com.project.whobusy.resources.plan_edit
import com.project.whobusy.resources.plan_in_space
import com.project.whobusy.resources.plan_new
import org.jetbrains.compose.resources.stringResource

@Composable
fun DesktopEventEditorScreen(
    state: EditorState,
    onEvent: (EditorCommand) -> Unit,
    onBack: () -> Unit,
    onSpace: (Int) -> Unit,
    sidebar: (@Composable () -> Unit)? = null,
) {
    val space = state.spaces.firstOrNull { it.id == state.form.spaceId }
    Row(Modifier.fillMaxSize()) {
        if (sidebar != null) {
            sidebar()
        } else {
            Column(Modifier.width(280.dp).fillMaxHeight().background(Color.White)) {
                Text(
                    stringResource(Res.string.board_my_plans),
                    Modifier.padding(24.dp),
                    style = MaterialTheme.typography.titleLarge,
                )
                LazyColumn(Modifier.weight(1f)) {
                    items(state.spaces, key = { it.id }) { item ->
                        WhoBusyListRow(
                            item.name,
                            spaceRoleTitle(item.myRole),
                            onClick = { if (!state.isSaving) onSpace(item.id) },
                            leading = { WhoBusyAvatar(item.name, spaceColor(item.id)) },
                        )
                    }
                }
            }
        }
        Column(Modifier.weight(1f).fillMaxHeight().background(WhoBusyColors.Background)) {
            Row(
                Modifier.fillMaxWidth().height(80.dp).background(WhoBusyColors.Header).padding(horizontal = 28.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        stringResource(if (state.eventId == null) Res.string.plan_new else Res.string.plan_edit),
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                    )
                    Text(
                        if (space == null) {
                            stringResource(Res.string.desktop_editor_subtitle)
                        } else {
                            stringResource(Res.string.plan_in_space, space.name)
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                WhoBusyIconButton(
                    UiIcon.Close,
                    stringResource(Res.string.action_cancel),
                    { if (!state.isSaving) onBack() },
                    tint = Color.White,
                )
            }
            if (state.isLoading || state.isSaving) LinearProgressIndicator(Modifier.fillMaxWidth())
            BoxWithConstraints(Modifier.fillMaxSize().padding(24.dp)) {
                val visibilityWidth = if (maxWidth >= 960.dp) 384.dp else 320.dp
                Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    DesktopEventFields(state, onEvent, Modifier.weight(1f).fillMaxHeight())
                    DesktopEventVisibilityPanel(
                        state,
                        onEvent,
                        onBack,
                        Modifier.width(visibilityWidth).fillMaxHeight(),
                    )
                }
            }
        }
    }
}

@Preview(name = "Личный план", widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopEventEditorPreview() {
    WhoBusyTheme { DesktopEventEditorScreen(EventPreviewData.editor, {}, {}, {}) }
}

@Preview(name = "План пространства", widthDp = 1360, heightDp = 960)
@Composable
private fun DesktopSpaceEventEditorPreview() {
    WhoBusyTheme {
        DesktopEventEditorScreen(
            EventPreviewData.editor.copy(form = EventPreviewData.editor.form.copy(spaceId = 1, shares = emptyList())),
            {},
            {},
            {},
        )
    }
}
