package com.project.whobusy.data

import com.project.whobusy.data.remote.ApiConfiguration
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ApiConfigurationTest {
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
