package com.project.whobusy.presentation.event.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.components.*
import com.project.whobusy.core.ui.theme.*
import com.project.whobusy.presentation.event.model.EventFormState
import java.time.LocalDate

@Composable
fun EventEditorScreen(
    state: EventFormState, onChange: (EventFormState) -> Unit, onSave: () -> Unit, onBack: () -> Unit,
    owner: String, editing: Boolean = false, saving: Boolean = false, error: String? = null,
) {
    Column(Modifier.fillMaxSize().background(WhoBusyColors.Header).statusBarsPadding()) {
        WhoBusyTopBar(if (editing) "Изменить событие" else "Новое событие", onBack)
        Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).navigationBarsPadding()
            .imePadding().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EventEditorFields(state, onChange)
            Text("Календарь: $owner", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            (state.validationError ?: error)?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            WhoBusyButton(if (editing) "Сохранить изменения" else "Создать событие", onSave, loading = saving)
        }
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun EventEditorPreview() {
    WhoBusyTheme { EventEditorScreen(EventFormState.initial(LocalDate.of(2026, 10, 15), null).copy(title = "Обсуждение релиза"),
        {}, {}, {}, "Александр Гайдуков") }
}
