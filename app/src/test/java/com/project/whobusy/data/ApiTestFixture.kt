package com.project.whobusy.data

import com.project.whobusy.data.remote.ApiConfiguration
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal class ApiTestFixture(
    private val response: (HttpRequestData) -> Pair<String, HttpStatusCode>,
) : AutoCloseable {
    val requests = mutableListOf<HttpRequestData>()
    val configuration = ApiConfiguration("https://calendar.example.test/api/")
    val client =
        HttpClient(
            MockEngine { request ->
                requests.add(request)
                val (body, status) = response(request)
                respond(body, status, headersOf(HttpHeaders.ContentType, "application/json"))
            },
        ) {
            expectSuccess = true
            install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
        }

    override fun close() = client.close()

    companion object {
        val userJson =
            """
            {"id":1,"email":"demo@whobusy.test","full_name":"Александр Иванов",
             "role":"user","color":"#4c5c9e","is_active":true}
            """.trimIndent()
        val tokenJson = """{"access_token":"test-jwt","token_type":"bearer"}"""
    }
}
