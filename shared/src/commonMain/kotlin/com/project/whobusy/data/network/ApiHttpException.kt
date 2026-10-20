package com.project.whobusy.data.network

import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject

class ApiHttpException(
    val status: HttpStatusCode,
    message: String,
) : Exception(message)

internal suspend fun HttpResponse.requireSuccess(): HttpResponse {
    if (status.value in 200..299) return this
    val message = try {
        val detail = Json.parseToJsonElement(bodyAsText()).jsonObject["detail"]
        (detail as? JsonPrimitive)?.content
    } catch (exception: kotlinx.coroutines.CancellationException) {
        throw exception
    } catch (_: Exception) {
        null
    }
    throw ApiHttpException(status, message ?: "API request failed (${status.value})")
}
