package com.retro99.books.ui.list

import com.retro99.base.ui.BaseIntent
import com.retro99.books.domain.model.BookDomainModel

sealed interface BooksListIntent : BaseIntent {
    data object OnRefresh : BooksListIntent
    data class OnBookClicked(val book: BookDomainModel) : BooksListIntent
}

