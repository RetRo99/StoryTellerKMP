package com.retro99.statistics.data.model

import com.retro99.database.api.statistics.DailyReadingTimeEntity
import com.retro99.statistics.domain.model.DailyReadingTimeDomainModel

fun DailyReadingTimeEntity.toDomain(): DailyReadingTimeDomainModel {
    return DailyReadingTimeDomainModel(
        dayStart = dayStart,
        totalDurationMs = totalDurationMs,
    )
}

