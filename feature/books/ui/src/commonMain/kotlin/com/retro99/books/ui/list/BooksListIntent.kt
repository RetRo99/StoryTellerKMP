package com.retro99.books.ui.list

import com.retro99.base.ui.BaseIntent

sealed interface BooksListIntent : BaseIntent {
    data object OnRefresh : BooksListIntent
    data class OnBookClicked(val bookUuid: String) : BooksListIntent
}

