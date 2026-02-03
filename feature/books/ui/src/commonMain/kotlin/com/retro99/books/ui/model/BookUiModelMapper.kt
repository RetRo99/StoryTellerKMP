package com.retro99.books.ui.model

import com.retro99.books.domain.model.BookDomainModel
import com.retro99.books.domain.model.PositionDomainModel
import com.retro99.books.domain.model.SeriesDomainModel

fun BookDomainModel.toUiModel(): BookUiModel = BookUiModel(
    uuid = uuid,
    title = title,
    subtitle = subtitle,
    coverUrl = coverUrl,
    authors = authors.map { it.name },
    series = series.map { it.toUiModel() },
    tags = tags.map { it.name },
    statusName = status?.name,
    rating = rating,
    description = description,
    hasEbook = ebook != null,
    hasAudiobook = audiobook != null,
    hasReadaloud = readaloud != null,
    ebookFilepath = ebook?.filepath,
    position = position?.toUiModel(),
)

fun PositionDomainModel.toUiModel(): PositionUiModel? {
    val href = locatorHref ?: return null
    val type = locatorType ?: return null
    val createdAt = createdAt ?: return null
    return PositionUiModel(
        uuid = uuid,
        createdAt = createdAt,
        href = href,
        type = type,
        title = locatorTitle,
        progression = progression,
        position = position,
        totalProgression = totalProgression,
        chapterIndex = chapterIndex,
        totalChapters = totalChapters,
    )
}

fun SeriesDomainModel.toUiModel(): SeriesUiModel = SeriesUiModel(
    uuid = uuid,
    name = name,
    position = position,
)

