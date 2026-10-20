package com.project.whobusy.di

internal actual fun browserOrigin(): String = js("globalThis.location.origin") as String
