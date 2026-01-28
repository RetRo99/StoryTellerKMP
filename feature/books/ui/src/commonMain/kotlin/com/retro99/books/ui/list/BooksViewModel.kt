package com.retro99.books.ui.list

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.fold
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBooksUseCase
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class BooksViewModel(
    @Provided private val getBooksUseCase: GetBooksUseCase,
) : BaseViewModel<BooksListViewState, BooksListIntent>() {

    override val initialState = BooksListViewState()

    init {
        loadBooks()
    }

    override fun onIntent(intent: BooksListIntent) {
        when (intent) {
            BooksListIntent.OnRefresh -> refreshBooks()
            is BooksListIntent.OnBookClicked -> handleBookClicked(intent.bookUuid)
        }
    }

    private fun loadBooks() {
        setLoading()
        viewModelScope.launch {
            getBooksUseCase().fold(
                success = { books ->
                    setState(BooksListViewState(books = books))
                },
                failure = { error ->
                    setError(error)
                },
            )
        }
    }

    private fun refreshBooks() {
        updateState { it.copy(isRefreshing = true) }
        viewModelScope.launch {
            getBooksUseCase().fold(
                success = { books ->
                    updateState { it.copy(books = books, isRefreshing = false) }
                },
                failure = { error ->
                    updateState { it.copy(isRefreshing = false) }
                    setError(error)
                },
            )
        }
    }

    private fun handleBookClicked(bookUuid: String) {
        // TODO: Navigate to book details
    }
}

