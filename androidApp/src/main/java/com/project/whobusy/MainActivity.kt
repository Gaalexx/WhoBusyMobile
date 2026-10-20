package com.project.whobusy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import com.project.whobusy.presentation.WhoBusyApp
import dev.zacsweers.metrox.viewmodel.LocalMetroViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appGraph = (application as WhoBusyApplication).appGraph
        setContent {
            CompositionLocalProvider(LocalMetroViewModelFactory provides appGraph.metroViewModelFactory) {
                WhoBusyApp()
            }
        }
    }
}
