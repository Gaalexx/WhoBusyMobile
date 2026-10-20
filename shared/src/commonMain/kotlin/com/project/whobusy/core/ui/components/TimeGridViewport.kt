package com.project.whobusy.core.ui.components

import kotlin.math.roundToInt

/** Pixel coordinates shared by hour lines, event edges and the current-time marker. */
data class TimeGridViewport(
    val hourHeightPx: Int,
    val originY: Int,
    val heightPx: Int,
) {
    fun minuteY(minute: Int): Int = originY + (minute * hourHeightPx / 60.0).roundToInt()

    fun eventHeight(startMinute: Int, endMinute: Int): Int = minuteY(endMinute) - minuteY(startMinute)

    fun isVisible(startMinute: Int, endMinute: Int): Boolean =
        minuteY(endMinute) > 0 && minuteY(startMinute) < heightPx
}
