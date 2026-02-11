package com.retro99.books.ui.detail

import com.retro99.base.result.AppError
import com.retro99.books.ui.model.BookUiModel
import com.retro99.reader.domain.model.BookType
import com.retro99.reader.domain.model.DownloadStateDomainModel

data class BookDetailViewState(
    val book: BookUiModel? = null,
    val isLoading: Boolean = true,
    val error: AppError? = null,
    val ebookDownloadState: DownloadStateDomainModel = DownloadStateDomainModel.Idle,
    val audiobookDownloadState: DownloadStateDomainModel = DownloadStateDomainModel.Idle,
    val readaloudDownloadState: DownloadStateDomainModel = DownloadStateDomainModel.Idle,
    val deleteConfirmationBookType: BookType? = null,
) {
    val isEbookCached: Boolean
        get() = ebookDownloadState is DownloadStateDomainModel.Cached

    val isAudiobookCached: Boolean
        get() = audiobookDownloadState is DownloadStateDomainModel.Cached

    val isReadaloudCached: Boolean
        get() = readaloudDownloadState is DownloadStateDomainModel.Cached

    val isEbookDownloading: Boolean
        get() = ebookDownloadState is DownloadStateDomainModel.Downloading

    val isAudiobookDownloading: Boolean
        get() = audiobookDownloadState is DownloadStateDomainModel.Downloading

    val isReadaloudDownloading: Boolean
        get() = readaloudDownloadState is DownloadStateDomainModel.Downloading
}

