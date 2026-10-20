package com.project.whobusy.time

import kotlin.js.JsModule

@JsModule("@js-joda/timezone")
external object JsJodaTimeZoneModule

private val jsJodaTz = JsJodaTimeZoneModule
