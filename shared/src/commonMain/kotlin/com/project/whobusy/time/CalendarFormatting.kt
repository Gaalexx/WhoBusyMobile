package com.project.whobusy.time

enum class CalendarLocale {
    Russian,
}

class DateTimeFormatter private constructor(private val pattern: String) {
    internal fun format(date: LocalDate, time: LocalTime = LocalTime(0, 0)): String {
        val values = mapOf(
            "yyyy" to date.year.toString().padStart(4, '0'),
            "LLLL" to russianMonthNames[date.month.ordinal],
            "MMMM" to russianMonthGenitives[date.month.ordinal],
            "EEEE" to russianWeekdayNames[date.dayOfWeek.ordinal],
            "dd" to date.day.toString().padStart(2, '0'),
            "d" to date.day.toString(),
            "MM" to (date.month.ordinal + 1).toString().padStart(2, '0'),
            "HH" to time.hour.toString().padStart(2, '0'),
            "mm" to time.minute.toString().padStart(2, '0'),
            "ss" to time.second.toString().padStart(2, '0'),
        )
        return tokens.replace(pattern.replace("'T'", "T")) { values.getValue(it.value) }
    }

    internal fun parseDate(value: String): LocalDate {
        require(pattern == "yyyyMMdd" && value.length == 8 && value.all(Char::isDigit))
        return LocalDate(value.substring(0, 4).toInt(), value.substring(4, 6).toInt(), value.substring(6, 8).toInt())
    }

    internal fun parseDateTime(value: String): LocalDateTime {
        require(pattern == "yyyyMMdd'T'HHmmss" && value.length == 15 && value[8] == 'T')
        val date = BASIC_ISO_DATE.parseDate(value.substring(0, 8))
        return date.atTime(
            value.substring(9, 11).toInt(),
            value.substring(11, 13).toInt(),
            value.substring(13, 15).toInt(),
        )
    }

    companion object {
        val BASIC_ISO_DATE: DateTimeFormatter = DateTimeFormatter("yyyyMMdd")

        fun ofPattern(pattern: String, locale: CalendarLocale = CalendarLocale.Russian): DateTimeFormatter {
            require(locale == CalendarLocale.Russian)
            require(pattern in supportedPatterns) { "Unsupported calendar format: $pattern" }
            return DateTimeFormatter(pattern)
        }

        private val supportedPatterns = setOf(
            "LLLL yyyy",
            "EEEE, d MMMM",
            "dd.MM",
            "HH:mm",
            "d MMMM",
            "d MMMM yyyy",
            "d MMMM, HH:mm",
            "d MMMM yyyy, HH:mm",
            "yyyyMMdd'T'HHmmss",
        )
        private val tokens = Regex("yyyy|LLLL|MMMM|EEEE|dd|MM|HH|mm|ss|d")
        private val russianMonthNames = listOf(
            "январь", "февраль", "март", "апрель", "май", "июнь",
            "июль", "август", "сентябрь", "октябрь", "ноябрь", "декабрь",
        )
        private val russianMonthGenitives = listOf(
            "января", "февраля", "марта", "апреля", "мая", "июня",
            "июля", "августа", "сентября", "октября", "ноября", "декабря",
        )
        private val russianWeekdayNames = listOf(
            "понедельник", "вторник", "среда", "четверг", "пятница", "суббота", "воскресенье",
        )
    }
}

fun LocalDate.format(formatter: DateTimeFormatter): String = formatter.format(this)

fun LocalDateTime.format(formatter: DateTimeFormatter): String = formatter.format(date, time)

fun kotlinx.datetime.LocalDate.Companion.parse(value: String, formatter: DateTimeFormatter): LocalDate =
    formatter.parseDate(value)

fun kotlinx.datetime.LocalDateTime.Companion.parse(value: String, formatter: DateTimeFormatter): LocalDateTime =
    formatter.parseDateTime(value)

fun Char.titlecase(locale: CalendarLocale): String {
    require(locale == CalendarLocale.Russian)
    return titlecase()
}

fun String.uppercase(locale: CalendarLocale): String {
    require(locale == CalendarLocale.Russian)
    return uppercase()
}
