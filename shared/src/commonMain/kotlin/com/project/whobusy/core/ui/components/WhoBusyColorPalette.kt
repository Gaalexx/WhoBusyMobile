package com.project.whobusy.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.resources.Res
import com.project.whobusy.resources.color_blue
import com.project.whobusy.resources.color_gold
import com.project.whobusy.resources.color_green
import com.project.whobusy.resources.color_purple
import com.project.whobusy.resources.color_red
import com.project.whobusy.resources.color_slate
import com.project.whobusy.resources.ui_color_blue
import com.project.whobusy.resources.ui_color_gold
import com.project.whobusy.resources.ui_color_green
import com.project.whobusy.resources.ui_color_purple
import com.project.whobusy.resources.ui_color_red
import com.project.whobusy.resources.ui_color_slate
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

private data class PaletteColor(
    val hex: String,
    val drawable: DrawableResource,
    val label: StringResource,
)

private val palette =
    listOf(
        PaletteColor("#2481CC", Res.drawable.color_blue, Res.string.ui_color_blue),
        PaletteColor("#6D4F9C", Res.drawable.color_purple, Res.string.ui_color_purple),
        PaletteColor("#357F59", Res.drawable.color_green, Res.string.ui_color_green),
        PaletteColor("#94691C", Res.drawable.color_gold, Res.string.ui_color_gold),
        PaletteColor("#72808D", Res.drawable.color_slate, Res.string.ui_color_slate),
        PaletteColor("#C84747", Res.drawable.color_red, Res.string.ui_color_red),
    )

@Composable
fun WhoBusyColorPalette(
    selectedColor: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(modifier.fillMaxWidth().selectableGroup()) {
        palette.forEach { color ->
            val selected = color.hex.equals(selectedColor, ignoreCase = true)
            val label = stringResource(color.label)
            Box(
                Modifier
                    .weight(1f)
                    .size(48.dp)
                    .selectable(selected, role = Role.RadioButton, onClick = { onSelect(color.hex) })
                    .semantics { contentDescription = label },
                contentAlignment = Alignment.Center,
            ) {
                if (selected) {
                    Box(Modifier.size(38.dp).border(2.dp, MaterialTheme.colorScheme.primary, CircleShape))
                }
                Image(painterResource(color.drawable), null, Modifier.size(30.dp))
            }
        }
    }
}

@Preview(widthDp = 358, showBackground = true)
@Composable
private fun ColorPalettePreview() {
    WhoBusyTheme { WhoBusyColorPalette("#2481CC", {}) }
}
