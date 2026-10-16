package com.project.whobusy.data.session

import com.project.whobusy.domain.model.CalendarUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionStore
    @Inject
    constructor() {
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

        fun clear() {
            session.value = null
        }
    }
