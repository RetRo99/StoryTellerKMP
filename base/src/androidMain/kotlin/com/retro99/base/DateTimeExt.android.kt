package com.retro99.base

import java.text.DateFormat
import java.util.Date

/**
 * Android implementation of formatCurrentTime.
 * Uses Java's DateFormat to format time according to the user's locale and
 * 12/24 hour preference set in system settings.
 */
actual fun formatCurrentTime(): String {
    val timeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)
    return timeFormat.format(Date())
}

