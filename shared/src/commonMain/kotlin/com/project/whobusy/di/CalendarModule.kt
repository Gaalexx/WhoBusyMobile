package com.project.whobusy.di

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
import com.project.whobusy.time.Clock
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

interface CalendarModule {
    @Provides
    @SingleIn(AppScope::class)
    fun provideWorkspaceRepository(
        configuration: AppConfiguration,
        mock: () -> MockWorkspaceRepository,
        remote: () -> RemoteWorkspaceRepository,
    ): WorkspaceRepository = if (configuration.useMockData) mock() else remote()

    @Provides
    fun provideLoginDefaults(configuration: AppConfiguration): LoginDefaults =
        if (configuration.useMockData) {
            LoginDefaults(DemoAccount.EMAIL, DemoAccount.PASSWORD)
        } else {
            LoginDefaults()
        }

    @Provides
    @SingleIn(AppScope::class)
    fun provideClock(): Clock = Clock.systemDefaultZone()

    @Provides
    @SingleIn(AppScope::class)
    fun provideApiConfiguration(configuration: AppConfiguration): ApiConfiguration =
        ApiConfiguration(configuration.apiBaseUrl)

    @Provides
    @SingleIn(AppScope::class)
    fun provideAuthRepository(
        configuration: AppConfiguration,
        mock: () -> MockAuthRepository,
        remote: () -> RemoteAuthRepository,
    ): AuthRepository = if (configuration.useMockData) mock() else remote()

    @Provides
    @SingleIn(AppScope::class)
    fun provideCalendarRepository(
        configuration: AppConfiguration,
        mock: () -> MockCalendarRepository,
        remote: () -> RemoteCalendarRepository,
    ): CalendarRepository = if (configuration.useMockData) mock() else remote()
}
