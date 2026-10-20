package com.project.whobusy.data.session

import com.project.whobusy.time.Clock

import com.project.whobusy.data.network.ApiHttpException
import com.project.whobusy.data.remote.ApiConfiguration
import com.project.whobusy.data.remote.api.AuthAPI
import com.project.whobusy.data.remote.dto.LoginRequestDTO
import com.project.whobusy.data.remote.dto.RegistrationRequestDTO
import com.project.whobusy.data.session.storage.RefreshTokenStore
import com.project.whobusy.di.ApplicationScope
import com.project.whobusy.domain.model.AuthenticationException
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.RegistrationDraft
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@SingleIn(AppScope::class)
@Inject
class RemoteSessionManager(
    private val api: AuthAPI,
    private val session: SessionStore,
    private val refreshTokens: RefreshTokenStore,
    private val configuration: ApiConfiguration,
    private val json: Json,
    private val clock: Clock,
    @param:ApplicationScope private val applicationScope: CoroutineScope,
) {
    private val mutex = Mutex()
    private var generation = 0L
    private var version = 0L
    private var expiresAt = Long.MAX_VALUE

    private data class Lease(val token: String, val generation: Long, val version: Long, val refresh: String?)

    @Serializable
    private data class StoredRefresh(val apiBaseUrl: String, val token: String)

    suspend fun login(identifier: String, password: String): CalendarUser = mutex.withLock {
        establish(api.login(LoginRequestDTO(identifier.trim().lowercase(), password)))
    }

    suspend fun register(draft: RegistrationDraft): CalendarUser = mutex.withLock {
        establish(api.register(RegistrationRequestDTO.fromDomain(draft)))
    }

    private suspend fun establish(tokens: AuthAPI.Tokens): CalendarUser {
        val user = api.getCurrentUser(tokens.access.accessToken).toDomain()
        persist(tokens.refresh)
        generation++
        publish(tokens, user)
        return user
    }

    suspend fun restore() = mutex.withLock {
        if (session.getUser() != null) return@withLock
        withContext(NonCancellable) {
            val refresh = if (api.browserManagedRefresh) null else readRefresh() ?: return@withContext
            try {
                val tokens = api.refresh(refresh)
                persist(tokens.refresh)
                val user = api.getCurrentUser(tokens.access.accessToken).toDomain()
                generation++
                publish(tokens, user)
            } catch (exception: ApiHttpException) {
                if (exception.status == HttpStatusCode.Unauthorized) invalidate() else throw exception
            }
        }
        currentCoroutineContext().ensureActive()
    }

    suspend fun logout() = mutex.withLock {
        withContext(NonCancellable) {
            if (api.browserManagedRefresh) {
                api.logout(null)
                clearLocal()
            } else {
                val refresh = readRefresh()
                clearLocal()
                if (refresh != null) {
                    applicationScope.launch {
                        try {
                            api.logout(refresh)
                        } catch (exception: CancellationException) {
                            throw exception
                        } catch (_: Exception) {
                            // Local logout is durable; server revocation is best effort when offline.
                        }
                    }
                }
            }
        }
        currentCoroutineContext().ensureActive()
    }

    suspend fun logoutAll() = mutex.withLock {
        withContext(NonCancellable) {
            var token = lease(false).token
            try {
                api.logoutAll(token)
            } catch (exception: ApiHttpException) {
                if (exception.status != HttpStatusCode.Unauthorized) throw exception
                renew()
                token = lease(false).token
                try {
                    api.logoutAll(token)
                } catch (retry: ApiHttpException) {
                    if (retry.status == HttpStatusCode.Unauthorized) {
                        invalidate()
                        throw AuthenticationException()
                    }
                    throw retry
                }
            }
            clearLocal()
        }
        currentCoroutineContext().ensureActive()
    }

    suspend fun <T> authorized(block: suspend (String) -> T): T =
        withCredentials(false) { token, _ -> block(token) }

    suspend fun <T> withCredentials(
        includeRefresh: Boolean,
        block: suspend (String, String?) -> T,
    ): T {
        val first = mutex.withLock { lease(includeRefresh) }
        try {
            val result = block(first.token, first.refresh)
            mutex.withLock { requireCurrentGeneration(first.generation) }
            return result
        } catch (exception: ApiHttpException) {
            if (exception.status != HttpStatusCode.Unauthorized) throw exception
        }
        val next = mutex.withLock {
            requireCurrentGeneration(first.generation)
            if (first.version == version) renew()
            lease(includeRefresh)
        }
        try {
            val result = block(next.token, next.refresh)
            mutex.withLock { requireCurrentGeneration(next.generation) }
            return result
        } catch (exception: ApiHttpException) {
            if (exception.status == HttpStatusCode.Unauthorized) {
                mutex.withLock {
                    requireCurrentGeneration(next.generation)
                    if (next.version == version) {
                        invalidate()
                    } else if (api.browserManagedRefresh) {
                        throw exception
                    }
                }
                throw AuthenticationException()
            }
            throw exception
        }
    }

    private fun requireCurrentGeneration(expected: Long) {
        if (expected == generation) return
        if (api.browserManagedRefresh) throw CancellationException("Browser session was replaced")
        throw AuthenticationException()
    }

    private suspend fun lease(includeRefresh: Boolean): Lease {
        if (session.getToken() == null || clock.millis() >= expiresAt - EXPIRY_MARGIN_MILLIS) renew()
        val refresh = if (includeRefresh && !api.browserManagedRefresh) readRefresh() else null
        if (includeRefresh && !api.browserManagedRefresh && refresh == null) {
            invalidate()
            throw AuthenticationException()
        }
        return Lease(session.getToken() ?: throw AuthenticationException(), generation, version, refresh)
    }

    private suspend fun renew() {
        withContext(NonCancellable) {
            val refresh = if (api.browserManagedRefresh) null else readRefresh() ?: run {
                invalidate()
                throw AuthenticationException()
            }
            try {
                val tokens = api.refresh(refresh)
                persist(tokens.refresh)
                val previousUser = session.getUser()
                val user = if (api.browserManagedRefresh || previousUser == null) {
                    api.getCurrentUser(tokens.access.accessToken).toDomain()
                } else {
                    previousUser
                }
                val identityChanged = previousUser != null && previousUser.id != user.id
                if (identityChanged) generation++
                publish(tokens, user)
                if (identityChanged) throw CancellationException("Browser session identity changed")
            } catch (exception: ApiHttpException) {
                if (exception.status == HttpStatusCode.Unauthorized) {
                    invalidate()
                    throw AuthenticationException()
                }
                throw exception
            }
        }
        currentCoroutineContext().ensureActive()
    }

    private fun publish(tokens: AuthAPI.Tokens, user: CalendarUser) {
        version++
        expiresAt = clock.millis() + tokens.access.expiresIn * 1_000
        session.save(tokens.access.accessToken, user)
    }

    private suspend fun persist(refresh: String?) {
        if (api.browserManagedRefresh) {
            require(refresh == null) { "Browser refresh credentials must not be persisted" }
            return
        }
        refreshTokens.write(json.encodeToString(StoredRefresh(configuration.endpoint(""), requireNotNull(refresh))))
    }

    private suspend fun readRefresh(): String? {
        val raw = refreshTokens.read() ?: return null
        val stored = try {
            json.decodeFromString<StoredRefresh>(raw)
        } catch (_: IllegalArgumentException) {
            refreshTokens.clear()
            return null
        }
        if (stored.apiBaseUrl != configuration.endpoint("")) {
            refreshTokens.clear()
            return null
        }
        return stored.token
    }

    private suspend fun clearLocal() {
        if (!api.browserManagedRefresh) refreshTokens.clear()
        generation++
        version++
        expiresAt = Long.MAX_VALUE
        session.clear()
    }

    private suspend fun invalidate() = withContext(NonCancellable) {
        try {
            if (!api.browserManagedRefresh) refreshTokens.clear()
        } finally {
            generation++
            version++
            expiresAt = Long.MAX_VALUE
            session.clear()
        }
    }

    companion object {
        private const val EXPIRY_MARGIN_MILLIS = 30_000L
    }
}
