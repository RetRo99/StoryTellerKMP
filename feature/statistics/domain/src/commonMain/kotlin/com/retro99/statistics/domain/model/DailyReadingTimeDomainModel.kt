package com.retro99.statistics.domain.model

/**
 * Represents aggregated reading time for a single day.
 */
data class DailyReadingTimeDomainModel(
    val dayStart: Long,
    val totalDurationMs: Long,
)

