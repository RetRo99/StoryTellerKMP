package com.retro99.books.ui.authors.detail

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBooksByAuthorUseCase
import com.retro99.books.ui.model.BookUiModel
import com.retro99.books.ui.model.toUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class AuthorDetailViewModel(
    @InjectedParam private val authorUuid: String,
    @InjectedParam private val authorName: String,
    @InjectedParam private val onNavigateToBookDetail: (book: BookUiModel) -> Unit,
    @InjectedParam private val onBack: () -> Unit,
    @Provided private val getBooksByAuthorUseCase: GetBooksByAuthorUseCase,
) : BaseViewModel<AuthorDetailViewState, AuthorDetailIntent>(
    AuthorDetailViewState(
        authorUuid = authorUuid,
        authorName = authorName,
    ),
) {

    init {
        observeBooks()
    }

    override fun onIntent(intent: AuthorDetailIntent) {
        when (intent) {
            AuthorDetailIntent.OnBackClicked -> onBack()
            AuthorDetailIntent.OnRefresh -> observeBooks()
            is AuthorDetailIntent.OnBookClicked -> onNavigateToBookDetail(intent.book)
        }
    }

    private fun observeBooks() {
        getBooksByAuthorUseCase(authorUuid)
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

