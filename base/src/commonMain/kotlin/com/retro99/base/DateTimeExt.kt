package com.retro99.base

import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

fun now() = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun nowMillis() = Clock.System.now().toEpochMilliseconds()

/**
 * Formats the current time according to the user's locale preferences.
 * This respects the user's 12-hour (AM/PM) or 24-hour time format setting.
 *
 * @return A formatted time string (e.g., "14:35" or "2:35 PM" depending on locale)
 */
expect fun formatCurrentTime(): String
