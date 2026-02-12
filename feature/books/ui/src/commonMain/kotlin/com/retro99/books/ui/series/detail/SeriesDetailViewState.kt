package com.retro99.books.ui.series.detail

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookUiModel

data class SeriesDetailViewState(
    val seriesUuid: String = "",
    val seriesName: String = "",
    val books: List<BookUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
)

