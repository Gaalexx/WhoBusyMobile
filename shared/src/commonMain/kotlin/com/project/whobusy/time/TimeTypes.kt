package com.project.whobusy.time

typealias LocalDate = kotlinx.datetime.LocalDate
typealias LocalDateTime = kotlinx.datetime.LocalDateTime
typealias LocalTime = kotlinx.datetime.LocalTime
typealias YearMonth = kotlinx.datetime.YearMonth
typealias DayOfWeek = kotlinx.datetime.DayOfWeek
typealias TimeZone = kotlinx.datetime.TimeZone
typealias Instant = kotlin.time.Instant
typealias Duration = kotlin.time.Duration

object ZoneOffset {
    val UTC: TimeZone = TimeZone.UTC
}
