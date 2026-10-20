package com.project.whobusy.di

import com.project.whobusy.BuildConfig
import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.repository.RemoteAuthRepository
import com.project.whobusy.data.repository.RemoteCalendarRepository
import com.project.whobusy.data.repository.RemoteWorkspaceRepository
import com.project.whobusy.domain.repository.AuthRepository
import com.project.whobusy.domain.repository.CalendarRepository
import com.project.whobusy.domain.repository.WorkspaceRepository
import com.project.whobusy.presentation.login.viewmodel.LoginDefaults
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import java.time.Clock

interface CalendarModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideWorkspaceRepository(
        mock: () -> MockWorkspaceRepository,
        remote: () -> RemoteWorkspaceRepository,
    ): WorkspaceRepository = if (BuildConfig.USE_MOCK_DATA) mock() else remote()

    @Provides
    fun provideLoginDefaults(): LoginDefaults =
        if (BuildConfig.USE_MOCK_DATA) {
            LoginDefaults(DemoAccount.EMAIL, DemoAccount.PASSWORD)
        } else {
            LoginDefaults()
        }

    @Provides
    @SingleIn(AppScope::class)
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @SingleIn(AppScope::class)
    fun provideApiConfiguration(): ApiConfiguration = ApiConfiguration(BuildConfig.API_BASE_URL)

    @Provides
    @SingleIn(AppScope::class)
    fun provideAuthRepository(
        mock: () -> MockAuthRepository,
        remote: () -> RemoteAuthRepository,
    ): AuthRepository = if (BuildConfig.USE_MOCK_DATA) mock() else remote()

    @Provides
    @SingleIn(AppScope::class)
    fun provideCalendarRepository(
        mock: () -> MockCalendarRepository,
        remote: () -> RemoteCalendarRepository,
    ): CalendarRepository = if (BuildConfig.USE_MOCK_DATA) mock() else remote()
}
