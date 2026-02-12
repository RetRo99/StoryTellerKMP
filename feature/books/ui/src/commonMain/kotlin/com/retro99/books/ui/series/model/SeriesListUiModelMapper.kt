package com.retro99.books.ui.series.model

import com.retro99.books.domain.model.SeriesDomainModel

fun SeriesDomainModel.toListUiModel(): SeriesListUiModel = SeriesListUiModel(
    uuid = uuid,
    name = name,
    featured = featured,
)

