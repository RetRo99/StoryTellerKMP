package com.retro99.books.ui.model

import com.retro99.books.domain.model.BookDomainModel
import com.retro99.books.domain.model.SeriesDomainModel

fun BookDomainModel.toUiModel(): BookUiModel = when (this) {
    is BookDomainModel.StorytellerBook -> BookUiModel.StorytellerBook(
        uuid = uuid,
        serverId = serverId,
        title = title,
        subtitle = subtitle,
        coverUrl = coverUrl,
        authors = authors.map { it.name },
        series = series.map { it.toUiModel() },
        tags = tags.map { it.name },
        statusName = status?.name,
        rating = rating,
        publicationDate = publicationDate,
        description = description,
        hasEbook = ebook?.filepath != null,
        hasAudiobook = audiobook?.filepath != null,
        hasReadaloud = readaloud?.filepath != null,
        ebookFilepath = ebook?.filepath,
        audiobookFilepath = audiobook?.filepath,
        readaloudFilepath = readaloud?.filepath,
    )

    is BookDomainModel.LocalBook -> BookUiModel.LocalBook(
        uuid = uuid,
        serverId = serverId,
        title = title,
        description = description,
        coverUrl = coverUrl,
        author = author,
        filePath = filePath,
        fileSize = fileSize,
        importedAt = importedAt,
        lastOpenedAt = lastOpenedAt,
        bookType = bookType,
        publicationDate = publicationDate,
    )
}

fun SeriesDomainModel.toUiModel(): SeriesUiModel = SeriesUiModel(
    uuid = uuid,
    name = name,
    position = position,
)
