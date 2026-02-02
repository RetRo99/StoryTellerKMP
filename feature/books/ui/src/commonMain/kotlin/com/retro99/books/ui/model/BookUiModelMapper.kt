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
    locator = position?.let { pos ->
        val href = pos.locatorHref ?: return@let null
        val type = pos.locatorType ?: return@let null
        LocatorUiModel(
            href = href,
            type = type,
            title = pos.locatorTitle,
            progression = pos.progression,
            position = pos.position,
            totalProgression = pos.totalProgression,
        )
    },
)

fun SeriesDomainModel.toUiModel(): SeriesUiModel = SeriesUiModel(
    uuid = uuid,
    name = name,
    position = position,
)

