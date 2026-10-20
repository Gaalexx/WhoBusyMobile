package com.project.whobusy.time

import kotlinx.datetime.toLocalDateTime

class Clock private constructor(
    private val source: kotlin.time.Clock,
    val zone: TimeZone,
) {
    fun instant(): Instant = source.now()

    fun millis(): Long = instant().toEpochMilliseconds()

    companion object {
        fun systemDefaultZone(): Clock = Clock(kotlin.time.Clock.System, TimeZone.currentSystemDefault())

        fun systemUTC(): Clock = Clock(kotlin.time.Clock.System, TimeZone.UTC)

        fun fixed(instant: Instant, zone: TimeZone): Clock = Clock(
            object : kotlin.time.Clock {
                override fun now(): Instant = instant
            },
            zone,
        )
    }
}

fun kotlinx.datetime.LocalDate.Companion.now(clock: Clock = Clock.systemDefaultZone()): LocalDate =
    clock.instant().toLocalDateTime(clock.zone).date

fun kotlinx.datetime.LocalDateTime.Companion.now(clock: Clock = Clock.systemDefaultZone()): LocalDateTime =
    clock.instant().toLocalDateTime(clock.zone)
