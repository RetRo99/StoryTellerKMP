package com.retro99.books.ui.series

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetSeriesUseCase
import com.retro99.books.ui.series.model.SeriesListUiModel
import com.retro99.books.ui.series.model.toListUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class SeriesListViewModel(
    @InjectedParam private val onNavigateToSeriesDetail: (series: SeriesListUiModel) -> Unit,
    @Provided private val getSeriesUseCase: GetSeriesUseCase,
) : BaseViewModel<SeriesListViewState, SeriesListIntent>(SeriesListViewState()) {

    init {
        observeSeries()
    }

    override fun onIntent(intent: SeriesListIntent) {
        when (intent) {
            SeriesListIntent.OnRefresh -> observeSeries()
            is SeriesListIntent.OnSeriesClicked -> onNavigateToSeriesDetail(intent.series)
        }
    }

    private fun observeSeries() {
        getSeriesUseCase()
            .onStart {
                updateState { it.copy(isLoading = true, error = null) }
            }
            .onEach { result ->
                result
                    .onSuccess { seriesList ->
                        updateState {
                            it.copy(
                                series = seriesList.map { series -> series.toListUiModel() },
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

