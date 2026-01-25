package com.retro99.base

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
