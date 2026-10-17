package com.project.whobusy.presentation.event.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.R
import com.project.whobusy.core.ui.components.WhoBusyButton
import com.project.whobusy.core.ui.components.WhoBusyTopBar
import com.project.whobusy.core.ui.theme.WhoBusyColors
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.event.model.EventFormState
import java.time.LocalDate

@Composable
fun EventEditorScreen(
    state: EventFormState,
    onChange: (EventFormState) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
    owner: String,
    editing: Boolean = false,
    saving: Boolean = false,
    @StringRes error: Int? = null,
) {
    Column(Modifier.fillMaxSize().background(WhoBusyColors.Header).statusBarsPadding()) {
        WhoBusyTopBar(stringResource(if (editing) R.string.screen_edit_event else R.string.screen_new_event), onBack)
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .navigationBarsPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            EventEditorFields(state, onChange)
            Text(
                stringResource(R.string.event_owner_calendar, owner),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            (state.validationError ?: error)?.let { Text(stringResource(it), color = MaterialTheme.colorScheme.error) }
            WhoBusyButton(
                stringResource(if (editing) R.string.action_save_event_changes else R.string.action_create_event),
                onSave,
                loading = saving,
            )
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun EventEditorPreview() {
    WhoBusyTheme {
        EventEditorScreen(
            EventFormState.initial(LocalDate.of(2026, 10, 15), null).copy(title = "Обсуждение релиза"),
            {},
            {},
            {},
            "Александр Гайдуков",
        )
    }
}
