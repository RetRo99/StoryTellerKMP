package com.retro99.books.ui.list

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBooksUseCase
import com.retro99.books.ui.model.BookUiModel
import com.retro99.books.ui.model.toUiModel
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class BooksViewModel(
    @InjectedParam private val onNavigateToBookDetail: (book: BookUiModel) -> Unit,
    @Provided private val getBooksUseCase: GetBooksUseCase,
) : BaseViewModel<BooksListViewState, BooksListIntent>(BooksListViewState()) {

    init {
        loadBooks()
    }

    override fun onIntent(intent: BooksListIntent) {
        when (intent) {
            BooksListIntent.OnRefresh -> refreshBooks()
            is BooksListIntent.OnBookClicked -> onNavigateToBookDetail(intent.book)
        }
    }

    private fun loadBooks() {
        updateState { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            getBooksUseCase().fold(
                success = { books ->
                    updateState {
                        it.copy(
                            books = books.map { book -> book.toUiModel() },
                            isLoading = false,
                            error = null,
                        )
                    }
                },
                failure = { error ->
                    updateState { it.copy(isLoading = false, error = error) }
                },
            )
        }
    }

    private fun refreshBooks() {
        updateState { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            getBooksUseCase().fold(
                success = { books ->
                    updateState {
                        it.copy(
                            books = books.map { book -> book.toUiModel() },
                            isRefreshing = false,
                        )
                    }
                },
                failure = { error ->
                    updateState { it.copy(isRefreshing = false, error = error) }
                },
            )
        }
    }
}

