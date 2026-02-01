package com.retro99.books.ui.detail

import com.retro99.base.result.AppError
import com.retro99.books.domain.model.BookDomainModel

data class BookDetailViewState(
    val book: BookDomainModel? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
)

