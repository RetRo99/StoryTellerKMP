package com.retro99.statistics.ui

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.log
import com.retro99.base.ui.BaseViewModel
import com.retro99.statistics.domain.usecase.GetReadingStatisticsUseCase
import com.retro99.statistics.ui.model.toUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class StatisticsViewModel(
    @InjectedParam private val onBack: () -> Unit,
    @Provided private val getReadingStatisticsUseCase: GetReadingStatisticsUseCase,
    @Provided private val analytics: Analytics,
) : BaseViewModel<StatisticsViewState, StatisticsIntent>(StatisticsViewState()) {

    init {
        loadStatistics()
    }

    override fun onIntent(intent: StatisticsIntent) {
        when (intent) {
            StatisticsIntent.OnRefresh -> loadStatistics()
            StatisticsIntent.OnBackClicked -> onBack()
        }
    }

    private fun loadStatistics() {
        getReadingStatisticsUseCase()
            .onStart {
                updateState { it.copy(isLoading = true, error = null) }
            }
            .onEach { result ->
                result
                    .onSuccess { statistics ->
                        updateState {
                            it.copy(
                                statistics = statistics.toUiModel(),
                                isLoading = false,
                                error = null,
                            )
                        }
                    }
                    .onFailure { error ->
                        error.log(analytics, "StatisticsViewModel: Failed to load statistics")
                        updateState {
                            it.copy(
                                isLoading = false,
                                error = error,
                            )
                        }
                    }
            }
            .launchIn(viewModelScope)
    }
}

