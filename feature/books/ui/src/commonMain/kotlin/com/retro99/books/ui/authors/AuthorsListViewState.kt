package com.retro99.books.ui.authors

import com.retro99.base.result.AppError
import com.retro99.books.ui.authors.model.AuthorListUiModel

data class AuthorsListViewState(
    val authors: List<AuthorListUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val error: AppError? = null,
)

