package com.project.whobusy.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.R
import com.project.whobusy.core.ui.theme.WhoBusyTheme

enum class TextFieldStyle { Plain, Filled }

@Composable
fun WhoBusyTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    placeholder: String? = null,
    style: TextFieldStyle = TextFieldStyle.Plain,
) {
    val filled = style == TextFieldStyle.Filled
    Column(modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier.fillMaxWidth().semantics {
                    contentDescription = label
                    if (error != null) error(error)
                },
            enabled = enabled,
            singleLine = singleLine,
            keyboardOptions = keyboardOptions,
            visualTransformation = visualTransformation,
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurface),
            cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
            decorationBox = { innerTextField ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (filled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = if (filled) {
                        BorderStroke(
                            1.dp,
                            if (error == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                        )
                    } else null,
                ) {
                    Column(
                        Modifier.heightIn(min = 64.dp).padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        Text(
                            label,
                            style = MaterialTheme.typography.bodySmall,
                            color = when {
                                error != null -> MaterialTheme.colorScheme.error
                                filled -> MaterialTheme.colorScheme.onSurface
                                else -> MaterialTheme.colorScheme.primary
                            },
                        )
                        Box {
                            if (value.isEmpty() && placeholder != null) {
                                Text(
                                    placeholder,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (filled) {
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                                    } else MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            innerTextField()
                        }
                    }
                }
            },
        )
        if (error != null) {
            Text(
                error,
                Modifier.padding(start = 12.dp, top = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Preview(name = "Поля на экране входа", showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun AuthTextFieldsPreview() {
    WhoBusyTheme {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(16.dp)) {
            WhoBusyTextField(
                "",
                {},
                stringResource(R.string.ui_email),
                style = TextFieldStyle.Filled,
            )
            WhoBusyTextField(
                "alex@example.com",
                {},
                stringResource(R.string.ui_email),
                style = TextFieldStyle.Filled,
            )
            WhoBusyTextField(
                "alex",
                {},
                stringResource(R.string.ui_email),
                error = stringResource(R.string.ui_error_email_invalid),
                style = TextFieldStyle.Filled,
            )
            WhoBusyTextField(
                "alex@example.com",
                {},
                stringResource(R.string.ui_email),
                enabled = false,
                style = TextFieldStyle.Filled,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TextFieldsPreview() {
    WhoBusyTheme {
        Column {
            WhoBusyTextField("alex@example.com", {}, stringResource(R.string.ui_email))
            WhoBusyTextField(
                "alex",
                {},
                stringResource(R.string.ui_email),
                error = stringResource(R.string.ui_error_email_invalid),
            )
        }
    }
}
