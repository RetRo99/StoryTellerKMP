package com.retro99.books.ui.detail

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookUiModel
import com.retro99.reader.domain.model.BookType

data class BookDetailViewState(
    val book: BookUiModel? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val isEbookCached: Boolean = false,
    val isAudiobookCached: Boolean = false,
    val isReadaloudCached: Boolean = false,
    val deleteConfirmationBookType: BookType? = null,
)

