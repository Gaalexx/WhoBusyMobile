package com.project.whobusy.di

import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.mock.MockCalendarRepository
import com.project.whobusy.data.mock.MockWorkspaceRepository
import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.repository.RemoteAuthRepository
import com.project.whobusy.data.repository.RemoteCalendarRepository
import com.project.whobusy.data.repository.RemoteWorkspaceRepository
import dev.zacsweers.metro.createGraphFactory
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class WebAppConfigurationTest {
    @Test
    fun productionUsesSameOriginApiWithBrowserManagedRemoteRepositories() = runTest {
        val configuration = webAppConfiguration("https://whobusy.ru", false, "")
        assertFalse(configuration.useMockData)
        assertFalse(configuration.allowLocalhostHttp)
        assertEquals("https://whobusy.ru/api/", configuration.apiBaseUrl)
        val client = HttpClient(MockEngine { error("Graph construction must not make requests") })
        try {
            val graph = createGraphFactory<WebAppGraph.Factory>().create(configuration, client, backgroundScope)
            assertIs<RemoteAuthRepository>(graph.auth)
            assertIs<RemoteCalendarRepository>(graph.calendar)
            assertIs<RemoteWorkspaceRepository>(graph.workspaces)
            assertTrue(graph.authApi.browserManagedRefresh)
        } finally {
            client.close()
        }
    }

    @Test
    fun localhostUsesTheSameOriginProxyWithoutAllowingRemoteCleartextHttp() {
        val configuration = webAppConfiguration("http://localhost:8080", false, "")
        assertTrue(configuration.allowLocalhostHttp)
        assertEquals(
            "http://localhost:8080/api/health",
            ApiConfiguration(configuration.apiBaseUrl, configuration.allowLocalhostHttp).endpoint("health"),
        )
        val override = webAppConfiguration("https://calendar.example.test", false, "https://api.example.test/api/")
        assertFalse(override.allowLocalhostHttp)
        assertEquals("https://api.example.test/api/", override.apiBaseUrl)
    }

    @Test
    fun explicitDemoSwitchSelectsAllMockRepositoriesTogether() = runTest {
        val configuration = webAppConfiguration("https://whobusy.ru", true, "")
        val client = HttpClient(MockEngine { error("Demo graph must not make requests") })
        try {
            val graph = createGraphFactory<WebAppGraph.Factory>().create(configuration, client, backgroundScope)
            assertIs<MockAuthRepository>(graph.auth)
            assertIs<MockCalendarRepository>(graph.calendar)
            assertIs<MockWorkspaceRepository>(graph.workspaces)
        } finally {
            client.close()
        }
    }
}
