package com.project.whobusy.data

import com.project.whobusy.data.network.AuthenticatedHttpClient
import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.remote.api.AuthAPI
import com.project.whobusy.data.session.RemoteSessionManager
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.data.session.storage.RefreshTokenStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.serialization.json.Json
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.Collections

internal class ApiTestFixture(
    private val response: (HttpRequestData) -> Pair<String, HttpStatusCode>,
) : AutoCloseable {
    val requests = Collections.synchronizedList(mutableListOf<HttpRequestData>())
    val configuration = ApiConfiguration("https://calendar.example.test/api/")
    val json = Json { ignoreUnknownKeys = true }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)
    var headersFor: (HttpRequestData, HttpStatusCode) -> Headers = { request, status ->
        headers {
            append(HttpHeaders.ContentType, "application/json")
            if (status.value in 200..299 && request.url.encodedPath in listOf(
                    "/api/auth/login", "/api/auth/register", "/api/auth/refresh",
                )
            ) append(
                HttpHeaders.SetCookie,
                "refresh_token=test-refresh; Path=/api/auth; Max-Age=2592000; HttpOnly; Secure; SameSite=Strict"
            )
        }
    }
    val client = HttpClient(
        MockEngine { request ->
            requests.add(request)
            val (body, status) = response(request)
            respond(body, status, headersFor(request, status))
        }
    ) {
        expectSuccess = false
        followRedirects = false
        install(ContentNegotiation) { json(json) }
    }

    fun sessionManager(
        session: SessionStore,
        store: RefreshTokenStore = MemoryRefreshTokenStore(),
        clock: Clock = Clock.fixed(Instant.parse("2026-10-17T09:00:00Z"), ZoneOffset.UTC),
    ) = RemoteSessionManager(AuthAPI(client, configuration), session, store, configuration, json, clock, scope)

    fun authenticatedClient(session: SessionStore): AuthenticatedHttpClient {
        val store =
            MemoryRefreshTokenStore("""{"apiBaseUrl":"https://calendar.example.test/api/","token":"test-refresh"}""")
        return AuthenticatedHttpClient(client, configuration, sessionManager(session, store))
    }

    override fun close() {
        scope.cancel()
        client.close()
    }

    companion object {
        val userJson = """
            {"id":1,"login":"demo","email":"demo@whobusy.test","first_name":"Александр","last_name":"Иванов",
             "full_name":"Александр Иванов","role":"user","color":"#4c5c9e","is_active":true}
        """.trimIndent()
        val tokenJson = """{"access_token":"test-jwt","token_type":"bearer","expires_in":900}"""
    }
}

internal class MemoryRefreshTokenStore(var value: String? = null) : RefreshTokenStore {
    override suspend fun read(): String? = value
    override suspend fun write(token: String) {
        value = token
    }

    override suspend fun clear() {
        value = null
    }
}
