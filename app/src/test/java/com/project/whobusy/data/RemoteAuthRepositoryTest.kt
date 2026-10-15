package com.project.whobusy.data

import com.project.whobusy.data.remote.api.AuthAPI
import com.project.whobusy.data.repository.RemoteAuthRepository
import com.project.whobusy.data.repository.repositoryResult
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.InvalidCredentialsException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteAuthRepositoryTest {
    @Test
    fun loginSendsJsonThenLoadsUserWithBearerToken() = runBlocking {
        ApiTestFixture { request ->
            val body = if (request.url.encodedPath.endsWith("login")) {
                ApiTestFixture.tokenJson
            } else {
                ApiTestFixture.userJson
            }
            body to HttpStatusCode.OK
        }.use { fixture ->
            val session = SessionStore()
            val repository = RemoteAuthRepository(AuthAPI(fixture.client, fixture.configuration), session)
            val user = repository.login(" DEMO@WHOBUSY.TEST ", "password").getOrThrow()
            val login = fixture.requests.first()
            val payload = Json.parseToJsonElement((login.body as TextContent).text).jsonObject

            assertEquals(HttpMethod.Post, login.method)
            assertEquals("/api/auth/login", login.url.encodedPath)
            assertEquals("demo@whobusy.test", payload.getValue("email").jsonPrimitive.content)
            assertEquals("password", payload.getValue("password").jsonPrimitive.content)
            assertNull(login.headers[HttpHeaders.Authorization])
            assertEquals("/api/auth/me", fixture.requests.last().url.encodedPath)
            assertEquals("Bearer test-jwt", fixture.requests.last().headers[HttpHeaders.Authorization])
            assertEquals(user, session.getUser())
            assertEquals("test-jwt", session.getToken())
        }
    }

    @Test
    fun rejectedCredentialsBecomeDomainError() = runBlocking {
        ApiTestFixture { "{}" to HttpStatusCode.Unauthorized }.use { fixture ->
            val session = SessionStore()
            val repository = RemoteAuthRepository(AuthAPI(fixture.client, fixture.configuration), session)

            val result = repository.login("demo@whobusy.test", "incorrect")

            assertTrue(result.exceptionOrNull() is InvalidCredentialsException)
            assertEquals(1, fixture.requests.size)
            assertNull(session.getToken())
        }
    }

    @Test
    fun failedCurrentUserRequestDoesNotStorePartialSession() = runBlocking {
        ApiTestFixture { request ->
            if (request.url.encodedPath.endsWith("login")) {
                ApiTestFixture.tokenJson to HttpStatusCode.OK
            } else {
                "{}" to HttpStatusCode.ServiceUnavailable
            }
        }.use { fixture ->
            val session = SessionStore()
            val repository = RemoteAuthRepository(AuthAPI(fixture.client, fixture.configuration), session)

            assertTrue(repository.login("demo@whobusy.test", "password").isFailure)
            assertNull(session.getToken())
            assertNull(session.getUser())
        }
    }

    @Test(expected = CancellationException::class)
    fun repositoryResultPreservesCoroutineCancellation() = runBlocking<Unit> {
        repositoryResult<Unit> { throw CancellationException("Cancelled") }
    }
}
