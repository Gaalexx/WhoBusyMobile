package com.project.whobusy.presentation.event.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyAvatar
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyListRow
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusySectionTitle
import com.project.whobusy.core.ui.components.WhoBusySelectionMark
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.domain.model.EventVisibility
import com.project.whobusy.presentation.event.preview.EventPreviewData
import com.project.whobusy.presentation.event.viewmodel.EditorCommand
import com.project.whobusy.presentation.event.viewmodel.EditorState
import com.project.whobusy.presentation.event.viewmodel.EventEditorViewModel

@Composable
fun EventVisibilityRoot(
    viewModel: EventEditorViewModel,
    onBack: () -> Unit,
    onChooseSpace: (Int) -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    EventVisibilityScreen(state, onBack, onChooseSpace)
}

@Composable
fun EventVisibilityScreen(
    state: EditorState,
    onBack: () -> Unit,
    onChooseSpace: (Int) -> Unit,
) {
    EventPage(
        stringResource(R.string.plan_who_sees),
        onBack,
        footer = {
            WhoBusyButton(stringResource(R.string.plan_done), onBack, Modifier.padding(16.dp))
        },
    ) {
        item(key = "header") {
            Column {
                WhoBusyNotice(
                    state.form.title,
                    stringResource(R.string.plan_personal_summary, planDates(state.form)),
                    Modifier.padding(16.dp),
                )
                Spacer(Modifier.height(32.dp))
                WhoBusySectionTitle(stringResource(R.string.plan_your_spaces), Modifier.padding(horizontal = 20.dp))
                Spacer(Modifier.height(16.dp))
                if (state.spaces.isEmpty()) {
                    Text(stringResource(R.string.plan_no_spaces), Modifier.padding(20.dp))
                }
            }
        }
        itemsIndexed(state.spaces, key = { _, space -> space.id }) { index, space ->
            val color = listOf(WhoBusyColors.Primary, WhoBusyColors.Purple, WhoBusyColors.Green)[index % 3]
            WhoBusyListRow(
                title = space.name,
                subtitle =
                    visibilityTitle(
                        state.form.shares
                            .firstOrNull { it.spaceId == space.id }
                            ?.visibility,
                    ),
                onClick = { onChooseSpace(space.id) },
                leading = { WhoBusyAvatar(space.name, color) },
            )
        }
        item(key = "visibility-notice") {
            Column {
                Text(
                    stringResource(R.string.plan_visibility_default),
                    Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                WhoBusyNotice(
                    stringResource(R.string.plan_spaces_overlap),
                    stringResource(R.string.plan_spaces_overlap_body),
                    Modifier.padding(horizontal = 16.dp),
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun SpaceVisibilityRoot(
    spaceId: Int,
    viewModel: EventEditorViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SpaceVisibilityScreen(
        spaceName =
            state.spaces
                .firstOrNull { it.id == spaceId }
                ?.name
                .orEmpty(),
        initial =
            state.form.shares
                .firstOrNull { it.spaceId == spaceId }
                ?.visibility,
        onBack = onBack,
        onApply = { value ->
            viewModel.onEvent(EditorCommand.Visibility(spaceId, value))
            onBack()
        },
    )
}

@Composable
fun SpaceVisibilityScreen(
    spaceName: String,
    initial: EventVisibility?,
    onBack: () -> Unit,
    onApply: (EventVisibility?) -> Unit,
) {
    var selected by rememberSaveable(spaceName, initial) { mutableStateOf(initial?.name) }
    EventPage(
        stringResource(R.string.plan_visibility_title, spaceName),
        onBack,
        footer = {
            WhoBusyButton(
                stringResource(R.string.plan_apply),
                {
                    onApply(selected?.let(EventVisibility::valueOf))
                },
                Modifier.padding(16.dp)
            )
        },
    ) {
        item {
            Column {
                Spacer(Modifier.height(36.dp))
                listOf(
                    Triple(
                        EventVisibility.Actors,
                        R.string.plan_visibility_actors,
                        R.string.plan_visibility_actors_body
                    ),
                    Triple(EventVisibility.Busy, R.string.plan_visibility_busy, R.string.plan_visibility_busy_body),
                    Triple(null, R.string.plan_visibility_none, R.string.plan_visibility_none_body),
                ).forEachIndexed { index, (value, title, subtitle) ->
                    if (index > 0) Spacer(Modifier.height(22.dp))
                    WhoBusyListRow(
                        stringResource(title),
                        stringResource(subtitle),
                        { selected = value?.name },
                        trailing = { WhoBusySelectionMark(selected == value?.name) },
                    )
                }
                Spacer(Modifier.height(54.dp))
                WhoBusyNotice(
                    stringResource(R.string.plan_visibility_public_notice),
                    stringResource(R.string.plan_visibility_public_body),
                    Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun VisibilityPreview() {
    WhoBusyTheme { EventVisibilityScreen(EventPreviewData.editor, {}, {}) }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun VisibilityChoicePreview() {
    WhoBusyTheme { SpaceVisibilityScreen("Momentum", EventVisibility.Actors, {}, {}) }
}
