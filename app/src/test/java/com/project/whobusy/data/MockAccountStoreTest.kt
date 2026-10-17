package com.project.whobusy.data

import com.project.whobusy.data.mock.DemoAccount
import com.project.whobusy.data.mock.MockAccountStore
import com.project.whobusy.data.mock.MockAuthRepository
import com.project.whobusy.data.session.SessionStore
import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.InvalidCredentialsException
import com.project.whobusy.domain.model.ManagedUserDraft
import com.project.whobusy.domain.model.ProfileDraft
import com.project.whobusy.domain.model.RegistrationDraft
import com.project.whobusy.domain.model.UserRole
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class MockAccountStoreTest {
    private val store = MockAccountStore()

    @Test
    fun loginAcceptsNormalizedLoginOrEmailAndKeepsAdminSeparate() {
        assertEquals(DemoAccount.user, store.authenticate(" DEMO ", DemoAccount.PASSWORD))
        assertEquals(DemoAccount.user, store.authenticate(" DEMO@WHOBUSY.TEST ", DemoAccount.PASSWORD))
        val admin = store.authenticate(" ADMIN ", "admin123")
        assertEquals(UserRole.ADMIN, admin.role)
        assertEquals(100, admin.id)
        assertEquals(
            5,
            store.users.value
                .distinctBy { it.id }
                .size,
        )
        assertThrows(InvalidCredentialsException::class.java) { store.authenticate("admin", DemoAccount.PASSWORD) }
    }

    @Test
    fun registrationWithoutEmailAuthenticatesAndPublishesSession() =
        runBlocking {
            val session = SessionStore()
            val auth = MockAuthRepository(session, store)
            val user = auth.register(registration().copy(login = " New.User ", email = " ")).getOrThrow()

            assertEquals("new.user", user.login)
            assertEquals("Петрова Елена", user.fullName)
            assertNull(user.email)
            assertEquals(UserRole.USER, user.role)
            assertEquals(user, session.getUser())
            assertEquals(user, store.authenticate(" NEW.USER ", "password"))
            assertTrue(store.users.value.contains(user))
            auth.logout()
            assertNull(session.getUser())
        }

    @Test
    fun registrationRejectsNormalizedDuplicatesWithoutChangingAccounts() {
        val before = store.users.value
        assertThrows(IllegalArgumentException::class.java) { store.register(registration().copy(login = " DEMO ")) }
        assertThrows(IllegalArgumentException::class.java) {
            store.register(registration().copy(email = " DEMO@WHOBUSY.TEST "))
        }
        assertEquals(before, store.users.value)
        val user = store.register(registration().copy(email = " New@Example.org "))
        assertEquals("new@example.org", user.email)
        assertEquals(user, store.authenticate(" NEW@EXAMPLE.ORG ", "password"))
    }

    @Test
    fun registrationEnforcesLoginNameAndPasswordLimits() {
        val invalid =
            listOf(
                registration().copy(login = "ab"),
                registration().copy(login = "a".repeat(33)),
                registration().copy(login = "кириллица"),
                registration().copy(login = "with space"),
                registration().copy(firstName = " "),
                registration().copy(lastName = " "),
                registration().copy(firstName = "a".repeat(101)),
                registration().copy(password = "123"),
                registration().copy(password = "a".repeat(129)),
                registration().copy(email = "invalid"),
            )
        invalid.forEach { draft -> assertThrows(IllegalArgumentException::class.java) { store.register(draft) } }
        assertEquals(5, store.users.value.size)
        val user = store.register(registration().copy(login = "a".repeat(32), password = "a".repeat(128)))
        assertEquals(user, store.authenticate(user.login, "a".repeat(128)))
    }

    @Test
    fun passwordChangeRequiresCurrentPasswordAndFailedChangesAreAtomic() {
        val user = store.register(registration().copy(email = "new@example.org"))
        val draft = profile(user).copy(email = null, password = "changed", currentPassword = "incorrect")
        assertThrows(IllegalArgumentException::class.java) { store.updateProfile(user.id, draft) }
        assertEquals(user, store.users.value.single { it.id == user.id })
        assertEquals(user, store.authenticate(user.login, "password"))

        val updated = store.updateProfile(user.id, draft.copy(currentPassword = "password", lastName = ""))

        assertNull(updated.email)
        assertEquals("Елена", updated.fullName)
        assertEquals(updated, store.authenticate(user.login, "changed"))
        assertThrows(InvalidCredentialsException::class.java) { store.authenticate(user.login, "password") }
    }

    @Test
    fun managedAccountCanBeUpdatedReactivatedAndResetWithoutCurrentPassword() {
        val created = store.saveUser(null, managed().copy(lastName = ""))
        assertEquals("Новый", created.fullName)
        val disabled = store.saveUser(created.id, managed().copy(isActive = false, password = null))
        assertFalse(disabled.isActive)
        assertThrows(IllegalStateException::class.java) { store.authenticate("new.user", "password") }
        val updated = store.saveUser(created.id, managed().copy(login = " NEW.LOGIN ", password = "reset"))
        assertEquals(created.id, updated.id)
        assertEquals(updated, store.authenticate("new.login", "reset"))
        assertThrows(InvalidCredentialsException::class.java) { store.authenticate("new.user", "password") }
    }

    @Test
    fun lastActiveAdminCannotBeDemotedOrDeactivatedAndAdminCannotDeleteSelf() {
        val admin = store.authenticate("admin", "admin123")
        assertThrows(IllegalArgumentException::class.java) {
            store.saveUser(admin.id, managed(admin).copy(role = UserRole.USER))
        }
        assertThrows(IllegalArgumentException::class.java) {
            store.saveUser(admin.id, managed(admin).copy(isActive = false))
        }
        assertThrows(IllegalArgumentException::class.java) { store.deleteUser(admin.id, admin.id) }
        assertEquals(admin, store.authenticate("admin", "admin123"))
        store.saveUser(null, managed().copy(role = UserRole.ADMIN))
        assertEquals(UserRole.USER, store.saveUser(admin.id, managed(admin).copy(role = UserRole.USER)).role)
    }

    @Test
    fun onlyActiveAdminCanDeleteOtherAccountsAndTheirCredentialsStopWorking() {
        val created = store.saveUser(null, managed())
        assertThrows(IllegalStateException::class.java) { store.deleteUser(created.id, DemoAccount.user.id) }
        store.deleteUser(created.id, 100)
        assertFalse(store.users.value.any { it.id == created.id })
        assertThrows(InvalidCredentialsException::class.java) { store.authenticate("new.user", "password") }
        assertThrows(NoSuchElementException::class.java) { store.deleteUser(created.id, 100) }
    }

    private fun registration() =
        RegistrationDraft(
            login = "new.user",
            firstName = " Елена ",
            lastName = " Петрова ",
            password = "password",
        )

    private fun profile(user: CalendarUser) =
        ProfileDraft(
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email,
            color = user.color,
        )

    private fun managed(user: CalendarUser? = null) =
        ManagedUserDraft(
            login = user?.login ?: "new.user",
            firstName = user?.firstName ?: "Новый",
            lastName = user?.lastName ?: "Пользователь",
            email = user?.email,
            role = user?.role ?: UserRole.USER,
            color = user?.color ?: "#2481CC",
            isActive = user?.isActive ?: true,
            password = if (user == null) "password" else null,
        )
}
