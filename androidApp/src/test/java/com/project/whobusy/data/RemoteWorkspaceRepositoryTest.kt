package com.project.whobusy.data

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.remote.api.WorkspaceAPI
import com.project.whobusy.data.repository.RemoteWorkspaceRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.BoardSettings
import com.project.whobusy.domain.model.ManagedUserDraft
import com.project.whobusy.domain.model.ProfileDraft
import com.project.whobusy.domain.model.SpaceRole
import com.project.whobusy.domain.model.UserRole
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class RemoteWorkspaceRepositoryTest {
    @Test
    fun listsSpacesWithIndependentMembershipRolesAndNullableInviteCode() = runBlocking {
        val spaces = SpaceRole.entries.mapIndexed { index, role ->
            spaceJson.replace("\"id\":1", "\"id\":${index + 1}")
                .replace("\"my_role\":\"admin\"", "\"my_role\":\"${role.name.lowercase()}\"")
                .replace("\"invite_code\":\"TEAM-KEY\"", "\"invite_code\":null")
        }.joinToString(prefix = "[", postfix = "]")
        ApiTestFixture { spaces to HttpStatusCode.OK }.use { fixture ->
            val result = repository(fixture).getSpaces().getOrThrow()

            assertEquals(SpaceRole.entries, result.map { it.myRole })
            assertEquals(4, result.first().memberCount)
            assertTrue(result.all { it.inviteCode == null })
            val request = fixture.requests.single()
            assertEquals(HttpMethod.Get, request.method)
            assertEquals("/api/spaces", request.url.encodedPath)
            assertEquals("Bearer test-jwt", request.headers[HttpHeaders.Authorization])
        }
    }

    @Test
    fun spaceLifecycleUsesContractPathsAndMethods() = runBlocking {
        ApiTestFixture { request ->
            when {
                request.url.encodedPath.endsWith("/leave") || request.method == HttpMethod.Delete ->
                    "" to HttpStatusCode.NoContent

                else -> spaceJson to HttpStatusCode.OK
            }
        }.use { fixture ->
            val repository = repository(fixture)

            assertEquals(SpaceRole.Admin, repository.createSpace(" Команда ").getOrThrow().myRole)
            repository.renameSpace(1, " Новая команда ").getOrThrow()
            repository.joinSpace(" TEAM-KEY ").getOrThrow()
            assertEquals("TEAM-KEY", repository.regenerateInviteCode(1).getOrThrow().inviteCode)
            repository.leaveSpace(1).getOrThrow()
            repository.deleteSpace(1).getOrThrow()

            assertEquals(
                listOf(
                    HttpMethod.Post to "/api/spaces",
                    HttpMethod.Patch to "/api/spaces/1",
                    HttpMethod.Post to "/api/spaces/join",
                    HttpMethod.Post to "/api/spaces/1/invite-code",
                    HttpMethod.Post to "/api/spaces/1/leave",
                    HttpMethod.Delete to "/api/spaces/1",
                ),
                fixture.requests.map { it.method to it.url.encodedPath },
            )
            assertEquals("Команда", fixture.requests[0].payload()["name"]?.jsonPrimitive?.content)
            assertEquals("Новая команда", fixture.requests[1].payload()["name"]?.jsonPrimitive?.content)
            assertEquals("TEAM-KEY", fixture.requests[2].payload()["code"]?.jsonPrimitive?.content)
        }
    }

    @Test
    fun boardSettingsUseSpaceIdsAndReturnServerFilteredSelection() = runBlocking {
        ApiTestFixture { request ->
            if (request.method == HttpMethod.Get) {
                """{"space_ids":[1],"show_own":true}""" to HttpStatusCode.OK
            } else {
                """{"space_ids":[2],"show_own":false}""" to HttpStatusCode.OK
            }
        }.use { fixture ->
            val repository = repository(fixture)

            assertEquals(BoardSettings(setOf(1)), repository.getBoardSettings().getOrThrow())
            val saved = repository.saveBoardSettings(BoardSettings(setOf(3, 2), showOwn = false)).getOrThrow()

            assertEquals(BoardSettings(setOf(2), showOwn = false), saved)
            assertTrue(fixture.requests.all { it.url.encodedPath == "/api/users/me/board" })
            val request = fixture.requests.last()
            assertEquals(HttpMethod.Put, request.method)
            assertEquals("[2,3]", request.payload()["space_ids"]?.jsonArray.toString())
            assertEquals("false", request.payload()["show_own"]?.jsonPrimitive?.content)
        }
    }

    @Test
    fun membersMapFloatingTimestampAndUpdateRoleAndRemoveByUserId() = runBlocking {
        ApiTestFixture { request ->
            when (request.method) {
                HttpMethod.Get -> "[$memberJson]" to HttpStatusCode.OK
                HttpMethod.Patch -> memberJson.replace("\"actor\"", "\"moderator\"") to HttpStatusCode.OK
                else -> "" to HttpStatusCode.NoContent
            }
        }.use { fixture ->
            val repository = repository(fixture)
            val member = repository.getMembers(1).getOrThrow().single()
            val updated = repository.updateMemberRole(1, 2, SpaceRole.Moderator).getOrThrow()
            repository.removeMember(1, 2).getOrThrow()

            assertEquals(2, member.userId)
            assertEquals("anna", member.login)
            assertEquals("Петрова Анна", member.fullName)
            assertEquals(SpaceRole.Actor, member.role)
            assertEquals(LocalDateTime.parse("2026-10-15T09:00:00.123456"), member.joinedAt)
            assertEquals(SpaceRole.Moderator, updated.role)
            assertEquals("moderator", fixture.requests[1].payload()["role"]?.jsonPrimitive?.content)
            assertEquals(
                listOf(
                    HttpMethod.Get to "/api/spaces/1/members",
                    HttpMethod.Patch to "/api/spaces/1/members/2",
                    HttpMethod.Delete to "/api/spaces/1/members/2",
                ),
                fixture.requests.map { it.method to it.url.encodedPath },
            )
        }
    }

    @Test
    fun incomingAndSpaceInvitationsMapRoleAndOptionalInviter() = runBlocking {
        ApiTestFixture { "[$invitationJson]" to HttpStatusCode.OK }.use { fixture ->
            val repository = repository(fixture)
            val incoming = repository.getInvitations().getOrThrow().single()
            val pending = repository.getSpaceInvitations(1).getOrThrow().single()

            assertEquals(incoming, pending)
            assertEquals(9, incoming.id)
            assertEquals(1, incoming.spaceId)
            assertEquals("Команда", incoming.spaceName)
            assertEquals(2, incoming.userId)
            assertEquals("anna", incoming.login)
            assertEquals(SpaceRole.Actor, incoming.role)
            assertNull(incoming.invitedByName)
            assertEquals(LocalDateTime.parse("2026-10-16T12:30:00"), incoming.createdAt)
            assertEquals(
                listOf("/api/invitations", "/api/spaces/1/invitations"),
                fixture.requests.map { it.url.encodedPath }
            )
        }
    }

    @Test
    fun invitationsCreateCancelAcceptAndDeclineUseSeparateEndpoints() = runBlocking {
        ApiTestFixture { request ->
            when {
                request.url.encodedPath.endsWith("/accept") -> spaceJson to HttpStatusCode.OK
                request.method == HttpMethod.Delete || request.url.encodedPath.endsWith("/decline") ->
                    "" to HttpStatusCode.NoContent

                else -> invitationJson to HttpStatusCode.Created
            }
        }.use { fixture ->
            val repository = repository(fixture)

            assertEquals(9, repository.invite(1, " ANNA ", SpaceRole.Actor).getOrThrow().id)
            repository.cancelInvitation(1, 9).getOrThrow()
            assertEquals(1, repository.acceptInvitation(9).getOrThrow().id)
            repository.declineInvitation(10).getOrThrow()

            assertEquals("anna", fixture.requests[0].payload()["login"]?.jsonPrimitive?.content)
            assertEquals("actor", fixture.requests[0].payload()["role"]?.jsonPrimitive?.content)
            assertEquals(
                listOf(
                    HttpMethod.Post to "/api/spaces/1/invitations",
                    HttpMethod.Delete to "/api/spaces/1/invitations/9",
                    HttpMethod.Post to "/api/invitations/9/accept",
                    HttpMethod.Post to "/api/invitations/10/decline",
                ),
                fixture.requests.map { it.method to it.url.encodedPath },
            )
        }
    }

    @Test
    fun profilePatchClearsEmailPreservesPasswordWhitespaceAndUpdatesSessionUser() = runBlocking {
        ApiTestFixture { userJson to HttpStatusCode.OK }.use { fixture ->
            val session = authenticatedSession()
            val user = repository(fixture, session).updateProfile(
                ProfileDraft(" Анна ", " Петрова ", null, "#8f4f68", " new password ", " old password "),
            ).getOrThrow()
            val request = fixture.requests.single()
            val payload = request.payload()

            assertEquals(HttpMethod.Patch, request.method)
            assertEquals("/api/auth/me", request.url.encodedPath)
            assertEquals("Анна", payload["first_name"]?.jsonPrimitive?.content)
            assertEquals("Петрова", payload["last_name"]?.jsonPrimitive?.content)
            assertEquals(JsonNull, payload["email"])
            assertEquals(" new password ", payload["password"]?.jsonPrimitive?.content)
            assertEquals(" old password ", payload["current_password"]?.jsonPrimitive?.content)
            assertEquals(user, session.getUser())
            assertEquals("test-jwt", session.getToken())
            assertEquals("anna", user.login)
            assertEquals("Анна", user.firstName)
            assertEquals("Петрова", user.lastName)
            assertNull(user.email)
        }
    }

    @Test
    fun rejectedProfilePatchPreservesExistingSessionUser() = runBlocking {
        ApiTestFixture { """{"detail":"Current password is incorrect"}""" to HttpStatusCode.BadRequest }.use { fixture ->
            val session = authenticatedSession()

            val result = repository(fixture, session).updateProfile(ProfileDraft("Имя", "", null, "#4c5c9e"))

            assertTrue(result.isFailure)
            assertEquals(DemoAccount.user, session.getUser())
            assertEquals("test-jwt", session.getToken())
            assertEquals(1, fixture.requests.size)
        }
    }

    @Test
    fun accountListAndActiveCreationUseAdminUserEndpoints() = runBlocking {
        ApiTestFixture { request ->
            if (request.method == HttpMethod.Get) "[$userJson]" to HttpStatusCode.OK
            else userJson to HttpStatusCode.Created
        }.use { fixture ->
            val repository = repository(fixture)

            assertEquals("anna", repository.getUsers().getOrThrow().single().login)
            repository.saveUser(null, managedDraft().copy(login = " ANNA ", password = " pass word ")).getOrThrow()
            val request = fixture.requests.last()
            val payload = request.payload()

            assertTrue(fixture.requests.all { it.url.encodedPath == "/api/users" })
            assertEquals(HttpMethod.Post, request.method)
            assertEquals("anna", payload["login"]?.jsonPrimitive?.content)
            assertEquals("user", payload["role"]?.jsonPrimitive?.content)
            assertEquals(" pass word ", payload["password"]?.jsonPrimitive?.content)
            assertFalse("is_active" in payload)
            assertEquals(2, fixture.requests.size)
        }
    }

    @Test
    fun inactiveAccountCreationUsesPostThenOnlyActivityPatch() = runBlocking {
        ApiTestFixture { request ->
            if (request.method == HttpMethod.Post) userJson to HttpStatusCode.Created
            else userJson.replace("\"is_active\":true", "\"is_active\":false") to HttpStatusCode.OK
        }.use { fixture ->
            val user = repository(fixture).saveUser(null, managedDraft().copy(isActive = false, password = "demo1234"))
                .getOrThrow()

            assertFalse(user.isActive)
            assertEquals(HttpMethod.Post, fixture.requests[0].method)
            assertEquals(HttpMethod.Patch, fixture.requests[1].method)
            assertEquals("/api/users/1", fixture.requests[1].url.encodedPath)
            assertEquals("""{"is_active":false}""", fixture.requests[1].payload().toString())
        }
    }

    @Test
    fun failedDeactivationDoesNotAutomaticallyRepeatSuccessfulCreation() = runBlocking {
        ApiTestFixture { request ->
            if (request.method == HttpMethod.Post) userJson to HttpStatusCode.Created
            else """{"detail":"Temporary failure"}""" to HttpStatusCode.ServiceUnavailable
        }.use { fixture ->
            val result =
                repository(fixture).saveUser(null, managedDraft().copy(isActive = false, password = "demo1234"))

            assertTrue(result.isFailure)
            assertEquals(listOf(HttpMethod.Post, HttpMethod.Patch), fixture.requests.map { it.method })
        }
    }

    @Test
    fun accountPatchClearsEmailOmitsUnchangedPasswordAndSynchronizesOwnAccount() = runBlocking {
        ApiTestFixture { userJson to HttpStatusCode.OK }.use { fixture ->
            val session = authenticatedSession()
            val updated = repository(fixture, session).saveUser(1, managedDraft()).getOrThrow()
            val request = fixture.requests.single()
            val payload = request.payload()

            assertEquals(HttpMethod.Patch, request.method)
            assertEquals("/api/users/1", request.url.encodedPath)
            assertEquals(JsonNull, payload["email"])
            assertFalse("password" in payload)
            assertEquals("true", payload["is_active"]?.jsonPrimitive?.content)
            assertEquals(updated, session.getUser())
            assertEquals("test-jwt", session.getToken())
        }
    }

    @Test
    fun accountDeleteAcceptsNoContent() = runBlocking {
        ApiTestFixture { "" to HttpStatusCode.NoContent }.use { fixture ->
            repository(fixture).deleteUser(2).getOrThrow()

            assertEquals(HttpMethod.Delete, fixture.requests.single().method)
            assertEquals("/api/users/2", fixture.requests.single().url.encodedPath)
        }
    }

    @Test
    fun lastAdministratorErrorRemainsFailureAndDoesNotInvalidateSession() = runBlocking {
        ApiTestFixture { """{"detail":"A space needs at least one admin"}""" to HttpStatusCode.BadRequest }.use { fixture ->
            val session = authenticatedSession()
            val result = repository(fixture, session).updateMemberRole(1, 1, SpaceRole.Actor)

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("A space needs at least one admin"))
            assertEquals("test-jwt", session.getToken())
            assertEquals(DemoAccount.user, session.getUser())
            assertEquals(1, fixture.requests.size)
        }
    }

    @Test(expected = CancellationException::class)
    fun cancellationIsNotConvertedToFailure() = runBlocking<Unit> {
        ApiTestFixture { throw CancellationException("Cancelled") }.use { fixture ->
            repository(fixture).getSpaces()
            error("Cancellation must propagate")
        }
    }

    private fun repository(fixture: ApiTestFixture, session: SessionStore = authenticatedSession()) =
        RemoteWorkspaceRepository(WorkspaceAPI(fixture.authenticatedClient(session)), session)

    private fun authenticatedSession() = SessionStore().apply { save("test-jwt", DemoAccount.user) }

    private fun managedDraft() = ManagedUserDraft("anna", "Анна", "Петрова", null, UserRole.USER, "#8f4f68", true)

    private fun HttpRequestData.payload() = Json.parseToJsonElement((body as TextContent).text).jsonObject

    companion object {
        private val spaceJson = """
            {"id":1,"name":"Команда","my_role":"admin","member_count":4,"invite_code":"TEAM-KEY"}
        """.trimIndent()
        private val memberJson = """
            {"user_id":2,"login":"anna","full_name":"Петрова Анна","color":"#8f4f68",
             "role":"actor","joined_at":"2026-10-15T09:00:00.123456"}
        """.trimIndent()
        private val invitationJson = """
            {"id":9,"space_id":1,"space_name":"Команда","user_id":2,"login":"anna",
             "full_name":"Петрова Анна","role":"actor","invited_by_name":null,"created_at":"2026-10-16T12:30:00"}
        """.trimIndent()
        private val userJson = """
            {"id":1,"login":"anna","email":null,"first_name":"Анна","last_name":"Петрова",
             "full_name":"Петрова Анна","role":"user","color":"#8f4f68","is_active":true}
        """.trimIndent()
    }
}
