package com.retro99.reader.data.model

import com.retro99.reader.domain.model.ReadingProgressDomainModel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReadingProgressLocalModel(
    @SerialName("book_uuid")
    val bookUuid: String,
    @SerialName("locator")
    val locator: String,
    @SerialName("progression")
    val progression: Float,
    @SerialName("last_read_at")
    val lastReadAt: String,
)

fun ReadingProgressLocalModel.toDomain(): ReadingProgressDomainModel {
    return ReadingProgressDomainModel(
        bookUuid = bookUuid,
        locator = locator,
        progression = progression,
        lastReadAt = lastReadAt,
    )
}

fun ReadingProgressDomainModel.toLocal(): ReadingProgressLocalModel {
    return ReadingProgressLocalModel(
        bookUuid = bookUuid,
        locator = locator,
        progression = progression,
        lastReadAt = lastReadAt,
    )
}

