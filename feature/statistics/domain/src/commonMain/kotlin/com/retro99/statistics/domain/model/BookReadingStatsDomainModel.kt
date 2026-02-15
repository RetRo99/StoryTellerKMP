package com.retro99.statistics.domain.model

/**
 * Represents aggregated reading statistics for a single book.
 */
data class BookReadingStatsDomainModel(
    val bookUuid: String,
    val bookTitle: String,
    val totalDurationMs: Long,
    val sessionCount: Long,
)

