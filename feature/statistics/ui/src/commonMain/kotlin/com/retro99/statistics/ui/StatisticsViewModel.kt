package com.retro99.statistics.ui

import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.analytics.api.Analytics
import com.retro99.base.result.log
import com.retro99.base.ui.BaseViewModel
import com.retro99.statistics.domain.model.StatisticsPeriod
import com.retro99.statistics.domain.usecase.GetBooksForPeriodUseCase
import com.retro99.statistics.domain.usecase.GetReadingStatisticsUseCase
import com.retro99.statistics.ui.model.toBookUiModel
import com.retro99.statistics.ui.model.toUiModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class StatisticsViewModel(
    @InjectedParam private val onBack: () -> Unit,
    @Provided private val getReadingStatisticsUseCase: GetReadingStatisticsUseCase,
    @Provided private val getBooksForPeriodUseCase: GetBooksForPeriodUseCase,
    @Provided private val analytics: Analytics,
) : BaseViewModel<StatisticsViewState, StatisticsIntent>(StatisticsViewState()) {

    init {
        loadStatistics()
    }

    override fun onIntent(intent: StatisticsIntent) {
        when (intent) {
            StatisticsIntent.OnRefresh -> loadStatistics()
            StatisticsIntent.OnBackClicked -> onBack()
            is StatisticsIntent.OnPeriodClicked -> loadBooksForPeriod(intent.period)
            StatisticsIntent.OnCurrentStreakClicked -> showCurrentStreak()
            StatisticsIntent.OnLongestStreakClicked -> showLongestStreak()
            StatisticsIntent.OnDismissDetail -> dismissDetail()
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

    private fun loadBooksForPeriod(period: StatisticsPeriod) {
        // Show loading state immediately
        updateState {
            it.copy(
                detailState = StatisticsDetailState(
                    period = period,
                    books = emptyList(),
                    isLoading = true,
                )
            )
        }

        viewModelScope.launch {
            getBooksForPeriodUseCase(period)
                .onSuccess { books ->
                    updateState {
                        it.copy(
                            detailState = StatisticsDetailState(
                                period = period,
                                books = books.map { book -> book.toBookUiModel() },
                                isLoading = false,
                            )
                        )
                    }
                }
                .onFailure { error ->
                    error.log(analytics, "StatisticsViewModel: Failed to load books for period")
                    updateState { it.copy(detailState = null) }
                }
        }
    }

    private fun showCurrentStreak() {
        val currentStreakDays = viewState.value.statistics?.currentStreakDays ?: return
        updateState {
            it.copy(
                streakDetailState = StreakDetailState(
                    streakType = StreakType.CURRENT,
                    days = currentStreakDays,
                )
            )
        }
    }

    private fun showLongestStreak() {
        val longestStreakDays = viewState.value.statistics?.longestStreakDays ?: return
        updateState {
            it.copy(
                streakDetailState = StreakDetailState(
                    streakType = StreakType.LONGEST,
                    days = longestStreakDays,
                )
            )
        }
    }

    private fun dismissDetail() {
        updateState { it.copy(detailState = null, streakDetailState = null) }
    }
}

