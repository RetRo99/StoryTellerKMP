package com.retro99.reader.data.model

import com.retro99.books.domain.model.BookType
import com.retro99.reader.domain.model.CurrentlyReadingDomainModel
import kotlinx.serialization.Serializable

/**
 * Local model for storing currently reading book info in preferences.
 */
@Serializable
data class CurrentlyReadingLocalModel(
    val bookUuid: String,
    val bookType: String,
    val bookTitle: String,
    val coverUrl: String?,
    val totalProgression: Double?,
)

fun CurrentlyReadingLocalModel.toDomain(): CurrentlyReadingDomainModel {
    return CurrentlyReadingDomainModel(
        bookUuid = bookUuid,
        bookType = BookType.fromValue(bookType),
        bookTitle = bookTitle,
        coverUrl = coverUrl,
        totalProgression = totalProgression,
    )
}

fun CurrentlyReadingDomainModel.toLocal(): CurrentlyReadingLocalModel {
    return CurrentlyReadingLocalModel(
        bookUuid = bookUuid,
        bookType = bookType.value,
        bookTitle = bookTitle,
        coverUrl = coverUrl,
        totalProgression = totalProgression,
    )
}

