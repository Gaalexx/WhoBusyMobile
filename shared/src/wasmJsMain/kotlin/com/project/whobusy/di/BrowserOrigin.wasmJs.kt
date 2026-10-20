package com.project.whobusy.di

import kotlin.JsFun

@JsFun("() => globalThis.location.origin")
private external fun readBrowserOrigin(): String

internal actual fun browserOrigin(): String = readBrowserOrigin()
