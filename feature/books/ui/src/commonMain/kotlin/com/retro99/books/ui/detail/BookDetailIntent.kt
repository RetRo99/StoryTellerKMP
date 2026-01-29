package com.retro99.books.ui.detail

import com.retro99.base.ui.BaseIntent

sealed interface BookDetailIntent : BaseIntent {
    data object OnBackClicked : BookDetailIntent
    data object OnRetryClicked : BookDetailIntent
    data class OnTagClicked(val tagName: String) : BookDetailIntent
    data class OnSeriesClicked(val seriesUuid: String) : BookDetailIntent
    data object OnReadEbookClicked : BookDetailIntent
    data object OnPlayAudiobookClicked : BookDetailIntent
}

