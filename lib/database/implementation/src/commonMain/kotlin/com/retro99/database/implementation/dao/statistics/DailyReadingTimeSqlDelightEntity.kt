package com.retro99.database.implementation.dao.statistics

import com.retro99.database.api.statistics.DailyReadingTimeEntity

/**
 * SQLDelight entity for daily reading time aggregation.
 */
data class DailyReadingTimeSqlDelightEntity(
    override val dayStart: Long,
    override val totalDurationMs: Long,
) : DailyReadingTimeEntity

