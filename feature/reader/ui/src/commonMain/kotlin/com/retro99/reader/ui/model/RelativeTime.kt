package com.retro99.reader.ui.model

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant

sealed class RelativeTime {
    data object JustNow : RelativeTime()

    data class MinutesAgo(val minutes: Int) : RelativeTime()

    data class HoursAgo(val hours: Int) : RelativeTime()

    data class DaysAgo(val days: Int) : RelativeTime()
}

private const val MINUTES_PER_HOUR = 60
private const val HOURS_PER_DAY = 24

fun relativeTimeFromIso(isoTimestamp: String, nowEpochMs: Long): RelativeTime {
    val instant = parseInstant(isoTimestamp)
    val diffMs = nowEpochMs - instant.toEpochMilliseconds()
    if (diffMs < 60_000L) return RelativeTime.JustNow

    val minutes = (diffMs / 60_000L).toInt()
    if (minutes < MINUTES_PER_HOUR) return RelativeTime.MinutesAgo(minutes)

    val hours = minutes / MINUTES_PER_HOUR
    if (hours < HOURS_PER_DAY) return RelativeTime.HoursAgo(hours)

    return RelativeTime.DaysAgo(hours / HOURS_PER_DAY)
}

private fun parseInstant(isoTimestamp: String): Instant =
    try {
        Instant.parse(isoTimestamp)
    } catch (_: IllegalArgumentException) {
        LocalDateTime.parse(isoTimestamp).toInstant(TimeZone.currentSystemDefault())
    }
