package com.project.whobusy.time

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant as nativeToInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

fun kotlinx.datetime.LocalDate.Companion.of(year: Int, month: Int, day: Int): LocalDate = LocalDate(year, month, day)

fun kotlinx.datetime.LocalDateTime.Companion.of(
    year: Int,
    month: Int,
    day: Int,
    hour: Int,
    minute: Int,
    second: Int = 0,
    nano: Int = 0,
): LocalDateTime = LocalDateTime(year, month, day, hour, minute, second, nano)

fun kotlinx.datetime.LocalTime.Companion.of(hour: Int, minute: Int, second: Int = 0): LocalTime =
    LocalTime(hour, minute, second)

val kotlinx.datetime.LocalTime.Companion.MIDNIGHT: LocalTime
    get() = LocalTime(0, 0)

val DayOfWeek.value: Int
    get() = isoDayNumber

fun LocalDate.toLocalDate(): LocalDate = this

fun LocalDateTime.toLocalDate(): LocalDate = date

fun LocalDateTime.toLocalTime(): LocalTime = time

fun LocalDate.toEpochDay(): Long = toEpochDays()

fun LocalDate.atStartOfDay(): LocalDateTime = atTime(0, 0)

fun LocalDate.atTime(time: LocalTime): LocalDateTime = LocalDateTime(this, time)

fun LocalDate.atTime(hour: Int, minute: Int, second: Int = 0): LocalDateTime =
    atTime(LocalTime(hour, minute, second))

fun LocalDate.plusDays(value: Long): LocalDate = plus(value, DateTimeUnit.DAY)

fun LocalDate.plusWeeks(value: Long): LocalDate = plus(value, DateTimeUnit.WEEK)

fun LocalDate.plusMonths(value: Long): LocalDate = plus(value, DateTimeUnit.MONTH)

fun LocalDate.minusDays(value: Long): LocalDate = plusDays(-value)

fun LocalDate.minusWeeks(value: Long): LocalDate = plusWeeks(-value)

fun LocalDate.minusMonths(value: Long): LocalDate = plusMonths(-value)

fun LocalDate.withDayOfMonth(value: Int): LocalDate = LocalDate(year, month, value)

fun LocalDateTime.plusDays(value: Long): LocalDateTime = date.plusDays(value).atTime(time)

fun LocalDateTime.plusWeeks(value: Long): LocalDateTime = date.plusWeeks(value).atTime(time)

fun LocalDateTime.plusMonths(value: Long): LocalDateTime = date.plusMonths(value).atTime(time)

fun LocalDateTime.plusHours(value: Long): LocalDateTime = plus(value.hours)

fun LocalDateTime.plusMinutes(value: Long): LocalDateTime = plus(value.minutes)

fun LocalDateTime.minusDays(value: Long): LocalDateTime = plusDays(-value)

fun LocalDateTime.minusMonths(value: Long): LocalDateTime = plusMonths(-value)

fun LocalDateTime.minusHours(value: Long): LocalDateTime = plusHours(-value)

fun LocalDateTime.withHour(value: Int): LocalDateTime =
    LocalDateTime(date, LocalTime(value, minute, second, nanosecond))

fun LocalDateTime.withMinute(value: Int): LocalDateTime =
    LocalDateTime(date, LocalTime(hour, value, second, nanosecond))

fun LocalDateTime.plus(duration: Duration): LocalDateTime =
    (nativeToInstant(TimeZone.UTC) + duration).toLocalDateTime(TimeZone.UTC)

fun LocalDateTime.minus(duration: Duration): LocalDateTime = plus(-duration)

fun kotlin.time.Duration.Companion.between(start: LocalDateTime, end: LocalDateTime): Duration =
    end.nativeToInstant(TimeZone.UTC) - start.nativeToInstant(TimeZone.UTC)

fun kotlin.time.Duration.Companion.ofHours(value: Long): Duration = value.hours

fun kotlin.time.Duration.Companion.ofDays(value: Long): Duration = value.days

fun kotlinx.datetime.YearMonth.Companion.of(year: Int, month: Int): YearMonth = YearMonth(year, month)

fun kotlinx.datetime.YearMonth.Companion.from(date: LocalDate): YearMonth = YearMonth(date.year, date.month)

fun YearMonth.atDay(day: Int): LocalDate = LocalDate(year, month, day)

fun YearMonth.atEndOfMonth(): LocalDate = lastDay

fun YearMonth.lengthOfMonth(): Int = numberOfDays

fun YearMonth.plusMonths(value: Long): YearMonth = plus(value, DateTimeUnit.MONTH)

fun YearMonth.minusMonths(value: Long): YearMonth = plusMonths(-value)

fun LocalDateTime.toInstant(zone: TimeZone): Instant = nativeToInstant(zone)

fun kotlin.time.Instant.Companion.ofEpochMilli(value: Long): Instant = Instant.fromEpochMilliseconds(value)

fun Instant.toEpochMilli(): Long = toEpochMilliseconds()

fun Instant.atOffset(zone: TimeZone): LocalDateTime = toLocalDateTime(zone)

fun LocalDate.toEpochMillisecondsUtc(): Long = atStartOfDay().toInstant(TimeZone.UTC).toEpochMilliseconds()

fun kotlinx.datetime.LocalDate.Companion.fromEpochMillisecondsUtc(value: Long): LocalDate =
    Instant.fromEpochMilliseconds(value).toLocalDateTime(TimeZone.UTC).date

enum class ChronoUnit {
    DAYS,
    WEEKS,
    MONTHS,
    MINUTES;

    fun between(start: LocalDate, end: LocalDate): Long = when (this) {
        DAYS -> end.toEpochDays() - start.toEpochDays()
        WEEKS -> DAYS.between(start, end) / 7
        MONTHS -> {
            val packedStart = (start.year * 12L + start.month.ordinal) * 32 + start.day
            val packedEnd = (end.year * 12L + end.month.ordinal) * 32 + end.day
            (packedEnd - packedStart) / 32
        }
        MINUTES -> DAYS.between(start, end) * 24 * 60
    }

    fun between(start: YearMonth, end: YearMonth): Long {
        require(this == MONTHS)
        return (end.year - start.year) * 12L + end.month.ordinal - start.month.ordinal
    }

    fun between(start: LocalDateTime, end: LocalDateTime): Long {
        require(this == MINUTES)
        return Duration.between(start, end).inWholeMinutes
    }
}

object TemporalAdjusters {
    fun previousOrSame(day: DayOfWeek): (LocalDate) -> LocalDate = { date ->
        date.minusDays((date.dayOfWeek.value - day.value).mod(7).toLong())
    }

    fun nextOrSame(day: DayOfWeek): (LocalDate) -> LocalDate = { date ->
        date.plusDays((day.value - date.dayOfWeek.value).mod(7).toLong())
    }
}

fun LocalDate.with(adjuster: (LocalDate) -> LocalDate): LocalDate = adjuster(this)
