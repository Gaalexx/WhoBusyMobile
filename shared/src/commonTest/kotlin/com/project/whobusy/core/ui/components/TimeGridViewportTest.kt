package com.project.whobusy.core.ui.components

import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.Test

class TimeGridViewportTest {
    @Test
    fun wholeHourEventMatchesBothHourLines() {
        val viewport = TimeGridViewport(hourHeightPx = 128, originY = -1_000, heightPx = 800)

        assertEquals(152, viewport.minuteY(9 * 60))
        assertEquals(280, viewport.minuteY(10 * 60))
        assertEquals(128, viewport.eventHeight(9 * 60, 10 * 60))
    }

    @Test
    fun touchingEventsShareTheirBoundaryAfterRounding() {
        val viewport = TimeGridViewport(hourHeightPx = 149, originY = 17, heightPx = 800)
        val firstTop = viewport.minuteY(9 * 60 + 15)
        val nextTop = viewport.minuteY(9 * 60 + 30)

        assertEquals(nextTop, firstTop + viewport.eventHeight(9 * 60 + 15, 9 * 60 + 30))
        assertEquals(
            viewport.minuteY(10 * 60),
            nextTop + viewport.eventHeight(9 * 60 + 30, 10 * 60),
        )
    }

    @Test
    fun shortEventKeepsItsActualDuration() {
        val viewport = TimeGridViewport(hourHeightPx = 64, originY = 0, heightPx = 500)

        assertEquals(16, viewport.eventHeight(9 * 60 + 15, 9 * 60 + 30))
        assertEquals(1, viewport.eventHeight(9 * 60, 9 * 60 + 1))
    }

    @Test
    fun roundingAbsoluteBoundariesDoesNotAccumulateDrift() {
        val viewport = TimeGridViewport(hourHeightPx = 149, originY = 0, heightPx = 500)

        assertEquals(listOf(0, 37, 75, 112, 149), listOf(0, 15, 30, 45, 60).map(viewport::minuteY))
        assertEquals(listOf(37, 38, 37, 37), (0 until 60 step 15).map { viewport.eventHeight(it, it + 15) })
        assertEquals(149, (0 until 60 step 15).sumOf { viewport.eventHeight(it, it + 15) })
    }

    @Test
    fun midnightIsTheLastDayBoundary() {
        val viewport = TimeGridViewport(hourHeightPx = 149, originY = 23, heightPx = 500)

        assertEquals(23 + 24 * 149, viewport.minuteY(1_440))
        assertEquals(149, viewport.eventHeight(23 * 60, 1_440))
        assertEquals(24 * 149, viewport.eventHeight(0, 1_440))
    }

    @Test
    fun longEventRemainsVisibleWhenItsStartHasScrolledAway() {
        val viewport = TimeGridViewport(hourHeightPx = 120, originY = -10 * 120, heightPx = 300)

        assertTrue(viewport.minuteY(8 * 60) < 0)
        assertTrue(viewport.isVisible(8 * 60, 12 * 60))
        assertTrue(viewport.isVisible(0, 1_440))
        assertTrue(viewport.isVisible(12 * 60, 13 * 60))
    }

    @Test
    fun eventsOutsideViewportAndTouchingItsEdgesAreNotVisible() {
        val viewport = TimeGridViewport(hourHeightPx = 120, originY = -10 * 120, heightPx = 300)

        assertFalse(viewport.isVisible(8 * 60, 9 * 60))
        assertFalse(viewport.isVisible(9 * 60, 10 * 60))
        assertFalse(viewport.isVisible(12 * 60 + 30, 13 * 60))
        assertFalse(viewport.isVisible(13 * 60, 14 * 60))
    }
}

