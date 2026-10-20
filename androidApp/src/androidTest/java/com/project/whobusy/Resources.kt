package com.project.whobusy

import kotlinx.coroutines.runBlocking
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

internal fun resourceText(resource: StringResource): String = runBlocking { getString(resource) }
