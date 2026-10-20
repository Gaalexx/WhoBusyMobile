package com.project.whobusy.data.remote.api

import com.project.whobusy.data.network.requireSuccess
import com.project.whobusy.data.network.NativeRefreshCookies
import com.project.whobusy.data.network.RefreshCookiePolicy
import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.remote.dto.LoginRequestDTO
import com.project.whobusy.data.remote.dto.RegistrationRequestDTO
import com.project.whobusy.data.remote.dto.TokenDTO
import com.project.whobusy.data.remote.dto.UserDTO
import dev.zacsweers.metro.Inject
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

@Inject
class AuthAPI(
    private val client: HttpClient,
    private val configuration: ApiConfiguration,
    private val refreshCookies: RefreshCookiePolicy = NativeRefreshCookies,
) {
    val browserManagedRefresh: Boolean get() = refreshCookies.browserManaged

    class Tokens(val access: TokenDTO, val refresh: String?) {
        override fun toString(): String = "Tokens(<redacted>)"
    }

    suspend fun login(request: LoginRequestDTO): Tokens = issue("auth/login", request)

    suspend fun register(request: RegistrationRequestDTO): Tokens = issue("auth/register", request)

    private suspend inline fun <reified T> issue(path: String, request: T): Tokens {
        val response = client.post(configuration.endpoint(path)) {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.requireSuccess()
        return readTokens(response)
    }

    suspend fun refresh(token: String?): Tokens = readTokens(
        client.post(configuration.endpoint("auth/refresh")) {
            refreshCookies.attachRefresh(this, token)
        }.requireSuccess(),
    )

    suspend fun logout(token: String?) {
        client.post(configuration.endpoint("auth/logout")) {
            refreshCookies.attachRefresh(this, token)
        }.requireSuccess()
    }

    suspend fun logoutAll(accessToken: String) {
        client.post(configuration.endpoint("auth/logout-all")) {
            bearerAuth(accessToken)
        }.requireSuccess()
    }

    suspend fun getCurrentUser(token: String): UserDTO = client.get(configuration.endpoint("auth/me")) {
        bearerAuth(token)
    }.requireSuccess().body()

    private suspend fun readTokens(response: io.ktor.client.statement.HttpResponse): Tokens {
        val access = response.body<TokenDTO>()
        require(access.tokenType.equals("bearer", ignoreCase = true)) { "Unsupported token type" }
        require(access.accessToken.isNotBlank() && access.expiresIn in 1..86_400) { "Invalid access token response" }
        return Tokens(access, refreshCookies.readRefresh(response.headers))
    }

    companion object {
        const val REFRESH_COOKIE = NativeRefreshCookies.REFRESH_COOKIE

        fun refreshCookie(token: String): String = NativeRefreshCookies.refreshCookie(token)
    }
}
