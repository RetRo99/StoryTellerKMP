package com.retro99.database.implementation.dao.statistics

import com.retro99.database.api.statistics.BookReadingStatsEntity

/**
 * SQLDelight entity for book reading statistics aggregation.
 */
data class BookReadingStatsSqlDelightEntity(
    override val bookUuid: String,
    override val bookTitle: String,
    override val totalDurationMs: Long,
    override val sessionCount: Long,
) : BookReadingStatsEntity

