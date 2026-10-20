package com.project.whobusy.data.network

import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.remote.api.AuthAPI
import com.project.whobusy.data.session.RemoteSessionManager
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod

@Inject
class AuthenticatedHttpClient(
    private val client: HttpClient,
    private val configuration: ApiConfiguration,
    private val sessions: RemoteSessionManager,
) {
    suspend fun request(
        path: String,
        method: HttpMethod = HttpMethod.Get,
        configure: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse = sessions.withCredentials(path == "auth/me" && method == HttpMethod.Patch) { token, refresh ->
        // The backend needs this cookie on profile password updates to preserve this session family.
        client.request(configuration.endpoint(path)) {
            this.method = method
            configure()
            bearerAuth(token)
            if (refresh != null) headers.append(HttpHeaders.Cookie, AuthAPI.refreshCookie(refresh))
        }.requireSuccess()
    }
}
