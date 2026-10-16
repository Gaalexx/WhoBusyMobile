package com.project.whobusy.presentation.calendar.model

enum class CalendarMode(
    val title: String,
) {
    Day("День"),
    Week("Неделя"),
    Month("Месяц"),
}

enum class CalendarTab(
    val title: String,
) {
    Calendar("Календарь"),
    Calendars("Календари"),
    Profile("Профиль"),
}
