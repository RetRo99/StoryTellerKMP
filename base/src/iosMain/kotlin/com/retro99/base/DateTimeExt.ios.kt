package com.retro99.base

import platform.Foundation.NSDate
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDateFormatterNoStyle
import platform.Foundation.NSDateFormatterShortStyle

/**
 * iOS implementation of formatCurrentTime.
 * Uses NSDateFormatter to format time according to the user's locale and
 * 12/24 hour preference set in system settings.
 */
actual fun formatCurrentTime(): String {
    val formatter = NSDateFormatter()
    formatter.timeStyle = NSDateFormatterShortStyle
    formatter.dateStyle = NSDateFormatterNoStyle
    return formatter.stringFromDate(NSDate())
}

