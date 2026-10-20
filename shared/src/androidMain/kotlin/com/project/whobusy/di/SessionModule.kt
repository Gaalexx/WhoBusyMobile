package com.project.whobusy.di

import com.project.whobusy.data.session.storage.EncryptedRefreshTokenStore
import com.project.whobusy.data.session.storage.RefreshTokenStore
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

interface SessionModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideRefreshTokenStore(store: EncryptedRefreshTokenStore): RefreshTokenStore = store

    @Provides
    @SingleIn(AppScope::class)
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
