package com.retro99.statistics.data.model

import com.retro99.database.api.statistics.BookReadingStatsEntity
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel

fun BookReadingStatsEntity.toDomain(): BookReadingStatsDomainModel {
    return BookReadingStatsDomainModel(
        bookUuid = bookUuid,
        bookTitle = bookTitle,
        totalDurationMs = totalDurationMs,
        sessionCount = sessionCount,
    )
}

