package com.retro99.books.ui.series.detail

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBooksBySeriesUseCase
import com.retro99.books.ui.model.BookUiModel
import com.retro99.books.ui.model.toUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SeriesDetailViewModel(
    @InjectedParam private val seriesUuid: String,
    @InjectedParam private val seriesName: String,
    @InjectedParam private val onNavigateToBookDetail: (book: BookUiModel) -> Unit,
    @InjectedParam private val onBack: () -> Unit,
    @Provided private val getBooksBySeriesUseCase: GetBooksBySeriesUseCase,
) : BaseViewModel<SeriesDetailViewState, SeriesDetailIntent>(
    SeriesDetailViewState(
        seriesUuid = seriesUuid,
        seriesName = seriesName,
    ),
) {

    init {
        observeBooks()
    }

    override fun onIntent(intent: SeriesDetailIntent) {
        when (intent) {
            SeriesDetailIntent.OnBackClicked -> onBack()
            SeriesDetailIntent.OnRefresh -> observeBooks()
            is SeriesDetailIntent.OnBookClicked -> onNavigateToBookDetail(intent.book)
        }
    }

    private fun observeBooks() {
        getBooksBySeriesUseCase(seriesUuid)
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

