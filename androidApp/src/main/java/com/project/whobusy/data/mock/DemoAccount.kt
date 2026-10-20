package com.project.whobusy.data.mock

import com.project.whobusy.domain.model.CalendarUser
import com.project.whobusy.domain.model.UserRole

object DemoAccount {
    const val EMAIL = "demo@whobusy.test"
    const val PASSWORD = "demo1234"

    val user =
        CalendarUser(
            id = 1,
            email = EMAIL,
            fullName = "Александр Гайдуков",
            role = UserRole.USER,
            color = "#2481CC",
            isActive = true,
        )
}
