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
    locator = position?.locator?.let { locator ->
        LocatorUiModel(
            href = locator.href,
            type = locator.type,
            progression = locator.locations?.progression,
            position = locator.locations?.position,
            totalProgression = locator.locations?.totalProgression,
        )
    },
)

fun SeriesDomainModel.toUiModel(): SeriesUiModel = SeriesUiModel(
    uuid = uuid,
    name = name,
    position = position,
)

