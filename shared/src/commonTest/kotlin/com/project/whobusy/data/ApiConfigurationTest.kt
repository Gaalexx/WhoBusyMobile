package com.project.whobusy.data

import com.project.whobusy.data.remote.ApiConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ApiConfigurationTest {
    @Test
    fun loopbackHttpIsAllowedOnlyWithExplicitWebOptIn() {
        listOf("localhost", "127.0.0.1", "[::1]").forEach { host ->
            val baseUrl = "http://$host:8080/api/"
            assertFailsWith<IllegalArgumentException> { ApiConfiguration(baseUrl).endpoint("health") }
            assertEquals(
                baseUrl + "health",
                ApiConfiguration(baseUrl, allowLocalhostHttp = true).endpoint("health"),
            )
        }
        listOf("localhost.example.test", "192.168.1.2", "whobusy.ru").forEach { host ->
            assertFailsWith<IllegalArgumentException> {
                ApiConfiguration("http://$host/api/", allowLocalhostHttp = true).endpoint("health")
            }
        }
    }

    @Test
    fun joinsApiPathWithoutDuplicateSlashes() {
        assertEquals(
            "https://calendar.example.test/api/auth/login",
            ApiConfiguration("https://calendar.example.test/api/").endpoint("/auth/login"),
        )
    }

    @Test
    fun rejectsUnprotectedOrIncompleteBaseUrls() {
        listOf(
            "http://calendar.example.test/api/",
            "calendar.example.test/api/",
            "https:///api/",
            "https://",
            "https://calendar.example.test/api/ invalid",
        ).forEach { baseUrl ->
            assertFailsWith<IllegalArgumentException>(baseUrl) {
                ApiConfiguration(baseUrl).endpoint("auth/login")
            }
        }
    }

    @Test
    fun rejectsCredentialsQueriesAndFragments() {
        listOf(
            "https://user:password@calendar.example.test/api/",
            "https://user@calendar.example.test/api/",
            "https://calendar.example.test/api/?query=value",
            "https://calendar.example.test/api/?",
            "https://calendar.example.test/api/#fragment",
            "https://calendar.example.test/api/#",
        ).forEach { baseUrl ->
            assertFailsWith<IllegalArgumentException>(baseUrl) {
                ApiConfiguration(baseUrl).endpoint("auth/login")
            }
        }
    }
}
