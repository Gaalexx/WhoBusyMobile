package com.project.whobusy.presentation.desktop

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DesktopLayoutTest {
    @Test
    fun phoneAndTabletBrowsersKeepTheMobileInterface() {
        assertFalse(useDesktopLayout(enabled = true, widthDp = 390f))
        assertFalse(useDesktopLayout(enabled = true, widthDp = 1024f))
        assertFalse(useDesktopLayout(enabled = true, widthDp = 1159.99f))
    }

    @Test
    fun desktopBrowsersUseTheWideLayoutAtTheBreakpoint() {
        assertTrue(useDesktopLayout(enabled = true, widthDp = 1160f))
        assertTrue(useDesktopLayout(enabled = true, widthDp = 1440f))
    }

    @Test
    fun androidHostsNeverEnableTheWebDesktopInterface() {
        assertFalse(useDesktopLayout(enabled = false, widthDp = 390f))
        assertFalse(useDesktopLayout(enabled = false, widthDp = 1440f))
    }
}
