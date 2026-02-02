package com.retro99.books.ui.detail

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookUiModel

data class BookDetailViewState(
    val book: BookUiModel? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
)

