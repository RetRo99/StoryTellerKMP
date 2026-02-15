package com.retro99.statistics.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.base.ui.LoadingScreen
import com.retro99.base.ui.compose.TextWrapper
import com.retro99.base.ui.compose.stringTextWrapper
import com.retro99.statistics.ui.model.ReadingStatisticsUiModel
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import resources.translations.general_back
import resources.translations.statistics_books_read
import resources.translations.statistics_current_streak
import resources.translations.statistics_longest_streak
import resources.translations.statistics_month
import resources.translations.statistics_title
import resources.translations.statistics_today
import resources.translations.statistics_total_sessions
import resources.translations.statistics_total_time
import resources.translations.statistics_week

@Composable
fun StatisticsScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = koinViewModel { parametersOf(onBack) },
) {
    BaseScreen(
        modifier = modifier,
        viewModel = viewModel,
    ) { viewState, intentDispatcher ->
        when {
            viewState.isLoading -> LoadingScreen()
            else -> StatisticsScreenContent(
                viewState = viewState,
                intentDispatcher = intentDispatcher,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatisticsScreenContent(
    viewState: StatisticsViewState,
    intentDispatcher: IntentDispatcher<StatisticsIntent>,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(StringRes.statistics_title)) },
                navigationIcon = {
                    IconButton(onClick = { intentDispatcher(StatisticsIntent.OnBackClicked) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(StringRes.general_back),
                        )
                    }
                },
            )
        },
        modifier = modifier,
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = false,
            onRefresh = { intentDispatcher(StatisticsIntent.OnRefresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            viewState.statistics?.let { stats ->
                StatisticsContent(stats = stats)
            }
        }
    }
}

@Composable
private fun StatisticsContent(
    stats: ReadingStatisticsUiModel,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Time Statistics Section
        item {
            Text(
                text = stringResource(StringRes.statistics_title),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    title = stringResource(StringRes.statistics_today),
                    value = stats.todayReadingTimeFormatted,
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = stringResource(StringRes.statistics_week),
                    value = stats.weekReadingTimeFormatted,
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    title = stringResource(StringRes.statistics_month),
                    value = stats.monthReadingTimeFormatted,
                    icon = Icons.Default.Schedule,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = stringResource(StringRes.statistics_total_time),
                    value = stats.totalReadingTimeFormatted,
                    icon = Icons.Default.AutoStories,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Reading Stats Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    title = stringResource(StringRes.statistics_books_read),
                    value = TextWrapper.Text(stats.totalBooksRead.toString()),
                    icon = Icons.Default.MenuBook,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = stringResource(StringRes.statistics_total_sessions),
                    value = TextWrapper.Text(stats.totalSessions.toString()),
                    icon = Icons.Default.AutoStories,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // Streak Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    title = stringResource(StringRes.statistics_current_streak),
                    value = TextWrapper.Resource(StringRes.statistics_days, stats.currentStreak),
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    title = stringResource(StringRes.statistics_longest_streak),
                    value = TextWrapper.Resource(StringRes.statistics_days, stats.longestStreak),
                    icon = Icons.Default.LocalFireDepartment,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: TextWrapper,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringTextWrapper(value),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}

