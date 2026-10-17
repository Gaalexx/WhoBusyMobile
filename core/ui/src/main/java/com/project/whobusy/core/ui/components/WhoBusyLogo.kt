package com.project.whobusy.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.whobusy.core.ui.R
import com.project.whobusy.core.ui.theme.WhoBusyTheme

@Composable
fun WhoBusyLogo(modifier: Modifier = Modifier.size(104.dp)) {
    Image(
        painter = painterResource(R.drawable.whobusy_logo),
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Fit,
    )
}

@Preview(showBackground = true)
@Composable
private fun LogoPreview() {
    WhoBusyTheme { WhoBusyLogo() }
}
