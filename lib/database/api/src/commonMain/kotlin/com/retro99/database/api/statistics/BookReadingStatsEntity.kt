package com.retro99.database.api.statistics

/**
 * Entity representing aggregated reading statistics for a single book.
 */
interface BookReadingStatsEntity {
    val bookUuid: String
    val bookTitle: String
    val totalDurationMs: Long
    val sessionCount: Long
}

