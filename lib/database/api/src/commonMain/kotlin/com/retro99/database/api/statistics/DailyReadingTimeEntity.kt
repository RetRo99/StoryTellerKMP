package com.retro99.database.api.statistics

/**
 * Entity representing aggregated reading time for a single day.
 */
interface DailyReadingTimeEntity {
    val dayStart: Long
    val totalDurationMs: Long
}

