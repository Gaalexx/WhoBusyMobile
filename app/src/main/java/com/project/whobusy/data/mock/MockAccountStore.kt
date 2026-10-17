package com.project.whobusy.data.mock

import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.InvalidCredentialsException
import com.project.whobusy.domain.model.ManagedUserDraft
import com.project.whobusy.domain.model.ProfileDraft
import com.project.whobusy.domain.model.RegistrationDraft
import com.project.whobusy.domain.model.UserRole
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockAccountStore
@Inject
constructor() {
    private val state = MutableStateFlow(seedUsers())
    val users = state.asStateFlow()

    private val passwords =
        state.value
            .associate { user ->
                user.id to if (user.id == ADMIN_ID) "admin123" else DemoAccount.PASSWORD
            }.toMutableMap()
    private var nextUserId = state.value.maxOf { it.id } + 1

    @Synchronized
    fun authenticate(
        identifier: String,
        password: String,
    ): CalendarUser {
        val normalized = identifier.trim().lowercase(Locale.ROOT)
        val user =
            state.value.firstOrNull {
                if ('@' in normalized) it.email == normalized else it.login == normalized
            }
        if (user == null || passwords[user.id] != password) throw InvalidCredentialsException()
        check(user.isActive) { "Account is disabled" }
        return user
    }

    @Synchronized
    fun register(draft: RegistrationDraft): CalendarUser {
        val login = normalizedLogin(draft.login)
        val email = normalizedEmail(draft.email)
        val firstName = normalizedName(draft.firstName)
        val lastName = normalizedName(draft.lastName)
        validatePassword(draft.password)
        ensureUnique(login, email)
        val user =
            CalendarUser(
                id = nextUserId++,
                email = email,
                fullName = fullName(firstName, lastName),
                role = UserRole.USER,
                color = "#4c5c9e",
                isActive = true,
                login = login,
                firstName = firstName,
                lastName = lastName,
            )
        passwords[user.id] = draft.password
        state.value += user
        return user
    }

    @Synchronized
    fun updateProfile(
        id: Int,
        draft: ProfileDraft,
    ): CalendarUser {
        val existing = requireUser(id)
        val firstName = normalizedName(draft.firstName)
        val lastName = normalizedName(draft.lastName, required = false)
        val email = normalizedEmail(draft.email)
        ensureUnique(existing.login, email, id)
        draft.password?.let {
            validatePassword(it)
            require(passwords[id] == draft.currentPassword) { "Current password is incorrect" }
        }
        val updated =
            existing.copy(
                firstName = firstName,
                lastName = lastName,
                fullName = fullName(firstName, lastName),
                email = email,
                color = draft.color,
            )
        draft.password?.let { passwords[id] = it }
        replace(updated)
        return updated
    }

    @Synchronized
    fun saveUser(
        id: Int?,
        draft: ManagedUserDraft,
    ): CalendarUser {
        val existing = id?.let(::requireUser)
        val login = normalizedLogin(draft.login)
        val email = normalizedEmail(draft.email)
        val firstName = normalizedName(draft.firstName)
        val lastName = normalizedName(draft.lastName, required = false)
        ensureUnique(login, email, id)
        val password =
            if (existing ==
                null
            ) {
                requireNotNull(draft.password) { "Password is required" }
            } else {
                draft.password
            }
        password?.let(::validatePassword)
        if (existing?.isActiveAdmin() == true && (draft.role != UserRole.ADMIN || !draft.isActive)) {
            ensureAnotherAdmin(existing.id)
        }
        val user =
            CalendarUser(
                id = existing?.id ?: nextUserId++,
                email = email,
                fullName = fullName(firstName, lastName),
                role = draft.role,
                color = draft.color,
                isActive = draft.isActive,
                login = login,
                firstName = firstName,
                lastName = lastName,
            )
        password?.let { passwords[user.id] = it }
        if (existing == null) state.value += user else replace(user)
        return user
    }

    @Synchronized
    fun deleteUser(
        id: Int,
        actingUserId: Int,
    ) {
        check(requireUser(actingUserId).isActiveAdmin()) { "Administrator access required" }
        require(id != actingUserId) { "You cannot delete yourself" }
        val user = requireUser(id)
        if (user.isActiveAdmin()) ensureAnotherAdmin(id)
        state.value = state.value.filterNot { it.id == id }
        passwords.remove(id)
    }

    private fun requireUser(id: Int): CalendarUser =
        state.value.firstOrNull { it.id == id } ?: throw NoSuchElementException("User not found")

    private fun replace(user: CalendarUser) {
        state.value = state.value.map { if (it.id == user.id) user else it }
    }

    private fun ensureUnique(
        login: String,
        email: String?,
        excludeId: Int? = null,
    ) {
        val others = state.value.filterNot { it.id == excludeId }
        require(others.none { it.login == login }) { "Login already taken" }
        require(email == null || others.none { it.email == email }) { "Email already registered" }
    }

    private fun ensureAnotherAdmin(id: Int) {
        require(
            state.value.any { it.id != id && it.isActiveAdmin() },
        ) { "The last administrator must remain active" }
    }

    private fun CalendarUser.isActiveAdmin() = isActive && role == UserRole.ADMIN

    private fun normalizedLogin(value: String): String =
        value.trim().lowercase(Locale.ROOT).also {
            require(LOGIN_PATTERN.matches(it)) { "Login must be 3-32 Latin letters, digits, '.', '_' or '-'" }
        }

    private fun normalizedEmail(value: String?): String? =
        value?.trim()?.lowercase(Locale.ROOT)?.takeIf { it.isNotEmpty() }?.also {
            require(EMAIL_PATTERN.matches(it)) { "Invalid email" }
        }

    private fun normalizedName(
        value: String,
        required: Boolean = true,
    ): String =
        value.trim().also {
            require(it.length in (if (required) 1 else 0)..100) { "Name must contain at most 100 characters" }
        }

    private fun validatePassword(password: String) {
        require(password.length in 4..128) { "Password must contain 4-128 characters" }
    }

    private fun fullName(
        firstName: String,
        lastName: String,
    ) = "$lastName $firstName".trim()

    private fun seedUsers(): List<CalendarUser> =
        (
            DemoCalendarData.users +
                CalendarUser(
                    id = ADMIN_ID,
                    email = null,
                    fullName = "Администратор",
                    role = UserRole.ADMIN,
                    color = "#4c5c9e",
                    isActive = true,
                    login = "admin",
                    firstName = "Администратор",
                    lastName = "",
                )
            ).distinctBy { it.id }

    private companion object {
        const val ADMIN_ID = 100
        val LOGIN_PATTERN = Regex("^[a-z0-9._-]{3,32}$")
        val EMAIL_PATTERN = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    }
}
