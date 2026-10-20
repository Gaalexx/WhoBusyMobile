package com.project.whobusy

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import com.project.whobusy.di.DemoAppGraph
import com.project.whobusy.presentation.WhoBusyApp
import dev.zacsweers.metro.createGraph
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

@Composable
fun App() {
    val graph = remember { createGraph<DemoAppGraph>() }
    CompositionLocalProvider(LocalMetroViewModelFactory provides graph.metroViewModelFactory) {
        WhoBusyApp()
    }
}
