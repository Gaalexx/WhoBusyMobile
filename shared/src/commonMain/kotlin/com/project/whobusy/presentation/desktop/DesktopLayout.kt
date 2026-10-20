package com.project.whobusy.presentation.desktop

internal const val DesktopLayoutMinWidthDp = 1160f

internal fun useDesktopLayout(enabled: Boolean, widthDp: Float): Boolean =
    enabled && widthDp >= DesktopLayoutMinWidthDp
