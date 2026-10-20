package com.project.whobusy.data.session

import com.project.whobusy.domain.model.CalendarUser
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

@SingleIn(AppScope::class)
@Inject
class SessionStore {
    private data class Session(
        val token: String,
        val user: CalendarUser,
    )

    private val session = MutableStateFlow<Session?>(null)
    val currentUser = session.map { it?.user }.distinctUntilChanged()

    fun getUser(): CalendarUser? = session.value?.user

    fun getToken(): String? = session.value?.token

    fun save(
        token: String,
        user: CalendarUser,
    ) {
        session.value = Session(token, user)
    }

    fun updateUser(user: CalendarUser) {
        session.update { current ->
            if (current?.user?.id == user.id) current.copy(user = user) else current
        }
    }

    fun clear() {
        session.value = null
    }
}
