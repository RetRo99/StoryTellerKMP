package com.retro99.books.ui.model

import com.retro99.books.domain.model.BookDomainModel
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
    position = position?.let { pos ->
        val href = pos.locatorHref ?: return@let null
        val type = pos.locatorType ?: return@let null
        val createdAt = pos.createdAt ?: return@let null
        PositionUiModel(
            uuid = pos.uuid,
            createdAt = createdAt,
            href = href,
            type = type,
            title = pos.locatorTitle,
            progression = pos.progression,
            position = pos.position,
            totalProgression = pos.totalProgression,
            chapterIndex = pos.chapterIndex,
            totalChapters = pos.totalChapters,
        )
    },
)

fun SeriesDomainModel.toUiModel(): SeriesUiModel = SeriesUiModel(
    uuid = uuid,
    name = name,
    position = position,
)

