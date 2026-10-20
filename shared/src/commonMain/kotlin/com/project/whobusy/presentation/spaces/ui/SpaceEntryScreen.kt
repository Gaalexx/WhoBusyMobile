package com.project.whobusy.presentation.spaces.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.UiIcon
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyIcon
import com.project.whobusy.core.ui.components.WhoBusyNotice
import com.project.whobusy.core.ui.components.WhoBusyScreenLayout
import com.project.whobusy.core.ui.components.WhoBusyTextField
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.spaces.viewmodel.SpacesCommand
import com.project.whobusy.presentation.spaces.viewmodel.SpacesState
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.spaces_create
import com.project.whobusy.resources.spaces_create_admin_body
import com.project.whobusy.resources.spaces_create_admin_title
import com.project.whobusy.resources.spaces_create_hint
import com.project.whobusy.resources.spaces_invite_key
import com.project.whobusy.resources.spaces_join
import com.project.whobusy.resources.spaces_join_hint
import com.project.whobusy.resources.spaces_join_title
import com.project.whobusy.resources.spaces_join_visitor_body
import com.project.whobusy.resources.spaces_join_visitor_title
import com.project.whobusy.resources.spaces_name
import com.project.whobusy.resources.spaces_new
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun CreateSpaceScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onBack: () -> Unit,
) {
    SpaceEntryScreen(state, false, onEvent, onBack)
}

@Composable
fun JoinSpaceScreen(
    state: SpacesState,
    onEvent: (SpacesCommand) -> Unit,
    onBack: () -> Unit,
) {
    SpaceEntryScreen(state, true, onEvent, onBack)
}

@Composable
private fun SpaceEntryScreen(
    state: SpacesState,
    joining: Boolean,
    onEvent: (SpacesCommand) -> Unit,
    onBack: () -> Unit,
) {
    WhoBusyScreenLayout(
        topBar = {
            WhoBusyTopBar(stringResource(if (joining) Res.string.spaces_join_title else Res.string.spaces_new), onBack)
        }
    ) {
        Column(Modifier.fillMaxSize().imePadding()) {
            SpacesFeedback(state.isLoading, state.error)
            Column(
                Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(Modifier.height(40.dp))
                Surface(
                    color =
                        if (joining) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                    shape = if (joining) RoundedCornerShape(28.dp) else CircleShape,
                ) {
                    Box(Modifier.size(88.dp), contentAlignment = Alignment.Center) {
                        WhoBusyIcon(
                            if (joining) UiIcon.Key else UiIcon.People,
                            modifier = Modifier.size(44.dp),
                            tint = if (joining) MaterialTheme.colorScheme.primary else Color.White,
                        )
                    }
                }
                Spacer(Modifier.height(if (joining) 56.dp else 44.dp))
                WhoBusyTextField(
                    value = if (joining) state.code else state.name,
                    onValueChange = {
                        onEvent(
                            if (joining) SpacesCommand.CodeChanged(it) else SpacesCommand.NameChanged(it),
                        )
                    },
                    label = stringResource(if (joining) Res.string.spaces_invite_key else Res.string.spaces_name),
                    enabled = !state.isSaving,
                )
                Spacer(Modifier.height(32.dp))
                WhoBusyNotice(
                    stringResource(
                        if (joining) Res.string.spaces_join_visitor_title else Res.string.spaces_create_admin_title,
                    ),
                    stringResource(
                        if (joining) Res.string.spaces_join_visitor_body else Res.string.spaces_create_admin_body,
                    ),
                )
                Text(
                    stringResource(if (joining) Res.string.spaces_join_hint else Res.string.spaces_create_hint),
                    Modifier.padding(horizontal = 4.dp, vertical = 24.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            WhoBusyButton(
                stringResource(if (joining) Res.string.spaces_join else Res.string.spaces_create),
                { onEvent(if (joining) SpacesCommand.Join else SpacesCommand.Create) },
                Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 24.dp),
                loading = state.isSaving,
                enabled = !state.isLoading,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun CreateSpacePreview() {
    WhoBusyTheme { CreateSpaceScreen(SpacesState(name = "Momentum"), {}, {}) }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun JoinSpacePreview() {
    WhoBusyTheme { JoinSpaceScreen(SpacesState(code = "q7Xv_3kM9pLa"), {}, {}) }
}
