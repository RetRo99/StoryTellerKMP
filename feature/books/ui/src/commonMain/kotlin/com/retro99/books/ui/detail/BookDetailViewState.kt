package com.retro99.books.ui.detail

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookUiModel
import com.retro99.books.domain.model.BookType
import com.retro99.reader.domain.model.DownloadState

data class BookDetailViewState(
    val book: BookUiModel? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val ebookDownloadState: DownloadState = DownloadState.Idle,
    val audiobookDownloadState: DownloadState = DownloadState.Idle,
    val readaloudDownloadState: DownloadState = DownloadState.Idle,
    val deleteConfirmationBookType: BookType? = null,
    val isFavorite: Boolean = false,
)

