package com.project.whobusy.presentation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.project.whobusy.core.ui.theme.WhoBusyTheme
import com.project.whobusy.presentation.navigation.AppNavHost

@Composable
fun WhoBusyApp(viewModel: AppViewModel = hiltViewModel()) {
    val user by viewModel.user.collectAsStateWithLifecycle()
    val view = LocalView.current
    SideEffect {
        val activity = view.context as? Activity
        if (activity != null) {
            WindowCompat.getInsetsController(activity.window, view).apply {
                isAppearanceLightStatusBars = user == null
                isAppearanceLightNavigationBars = true
            }
        }
    }
    WhoBusyTheme { AppNavHost(user?.id) }
}
