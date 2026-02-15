package com.retro99.statistics.data.model

import com.retro99.database.api.statistics.BookReadingStatsEntity
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel

fun BookReadingStatsEntity.toDomain(baseUrl: String?): BookReadingStatsDomainModel {
    return BookReadingStatsDomainModel(
        bookUuid = bookUuid,
        bookTitle = bookTitle,
        coverUrl = baseUrl?.let { "$it/api/v2/books/$bookUuid/cover" },
        totalDurationMs = totalDurationMs,
        sessionCount = sessionCount,
    )
}

