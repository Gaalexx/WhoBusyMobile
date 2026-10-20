package com.project.whobusy.di

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metrox.viewmodel.ViewModelGraph

@DependencyGraph(AppScope::class)
interface AppGraph : CalendarModule, NetworkModule, SessionModule, ViewModelGraph {
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(@Provides @ApplicationContext context: Context): AppGraph
    }
}
