package com.retro99.statistics.data.model

import com.retro99.base.url.CoverUrlBuilder
import com.retro99.database.api.statistics.BookReadingStatsEntity
import com.retro99.statistics.domain.model.BookReadingStatsDomainModel

fun BookReadingStatsEntity.toDomain(baseUrl: String?): BookReadingStatsDomainModel {
    return BookReadingStatsDomainModel(
        bookUuid = bookUuid,
        bookTitle = bookTitle,
        coverUrl = CoverUrlBuilder.buildCoverUrl(baseUrl, bookUuid),
        totalDurationMs = totalDurationMs,
        sessionCount = sessionCount,
    )
}

