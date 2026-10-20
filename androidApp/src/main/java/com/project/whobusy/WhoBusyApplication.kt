package com.project.whobusy

import android.app.Application
import com.project.whobusy.di.AppConfiguration
import com.project.whobusy.di.AppGraph
import dev.zacsweers.metro.createGraphFactory

class WhoBusyApplication : Application() {
    val appGraph: AppGraph by lazy {
        createGraphFactory<AppGraph.Factory>().create(
            applicationContext,
            AppConfiguration(BuildConfig.USE_MOCK_DATA, BuildConfig.API_BASE_URL),
        )
    }
}
