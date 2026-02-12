package com.retro99.books.ui.list

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.log
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBooksUseCase
import com.retro99.books.ui.model.BookUiModel
import com.retro99.books.ui.model.toUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class BooksViewModel(
    @InjectedParam private val onNavigateToBookDetail: (book: BookUiModel) -> Unit,
    @Provided private val getBooksUseCase: GetBooksUseCase,
    @Provided private val analytics: Analytics,
) : BaseViewModel<BooksListViewState, BooksListIntent>(BooksListViewState()) {

    init {
        observeBooks()
    }

    override fun onIntent(intent: BooksListIntent) {
        when (intent) {
            BooksListIntent.OnRefresh -> Unit // Flow automatically refreshes on start
            is BooksListIntent.OnBookClicked -> onNavigateToBookDetail(intent.book)
        }
    }

    private fun observeBooks() {
        getBooksUseCase()
            .onStart {
                updateState { it.copy(isLoading = true, error = null) }
            }
            .onEach { result ->
                result
                    .onSuccess { books ->
                        updateState {
                            it.copy(
                                books = books.map { book -> book.toUiModel() },
                                isLoading = false,
                                isRefreshing = false,
                                error = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        error.log(analytics, "BooksViewModel: Failed to load books")
                        updateState {
                            it.copy(
                                isLoading = false,
                                isRefreshing = false,
                                error = error,
                            )
                        }
                    }
            }
            .launchIn(viewModelScope)
    }
}

