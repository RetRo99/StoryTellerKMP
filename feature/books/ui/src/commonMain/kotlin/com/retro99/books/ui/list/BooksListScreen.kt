package com.retro99.books.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.books.ui.components.BookFilterChipsRow
import com.retro99.books.ui.components.BookGridCard
import com.retro99.books.ui.components.BookItemCard
import com.retro99.books.ui.components.BookSearchBar
import com.retro99.books.ui.components.BookSortSelector
import com.retro99.books.ui.model.BookLibrarySection
import com.retro99.books.ui.model.BookListViewMode
import com.retro99.books.ui.model.BookUiModel
import com.retro99.translations.StringRes
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import resources.translations.books_empty_filtered_subtitle
import resources.translations.books_empty_filtered_title
import resources.translations.books_empty_subtitle
import resources.translations.books_empty_title
import resources.translations.books_importing
import resources.translations.books_reset_filters
import resources.translations.books_series_with_position

@Composable
fun BooksListScreen(
    onNavigateToBookDetail: (book: BookUiModel) -> Unit,
    modifier: Modifier = Modifier,
    headerContent: @Composable (() -> Unit)? = null,
    viewModel: BooksListViewModel = koinViewModel { parametersOf(onNavigateToBookDetail) },
) {
    BaseScreen(
        modifier = modifier,
        viewModel = viewModel,
    ) { viewState, intentDispatcher ->
        BooksListScreenContent(
            viewState = viewState,
            searchFieldState = viewModel.searchFieldState,
            intentDispatcher = intentDispatcher,
            modifier = modifier,
            headerContent = headerContent,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BooksListScreenContent(
    viewState: BooksListViewState,
    searchFieldState: TextFieldState,
    intentDispatcher: IntentDispatcher<BooksListIntent>,
    modifier: Modifier = Modifier,
    headerContent: @Composable (() -> Unit)? = null,
) {
    val filePickerLauncher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("epub")),
        mode = PickerMode.Single,
    ) { file ->
        file?.let {
            intentDispatcher(BooksListIntent.OnImportBook(it))
        }
    }

    val listState = rememberLazyListState()

    // Scroll to top when sort or filters change
    LaunchedEffect(viewState.sortConfig, viewState.filterState.activeQuickFilters) {
        listState.scrollToItem(0)
    }

    if (viewState.isImporting) {
        ImportingDialog()
    }

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
                SmallFloatingActionButton(
                    onClick = { filePickerLauncher.launch() },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = null,
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                FloatingActionButton(
                    onClick = { intentDispatcher(BooksListIntent.OnSearchToggled) },
                ) {
                    Icon(
                        imageVector = if (viewState.isSearchVisible) {
                            Icons.Filled.Close
                        } else {
                            Icons.Filled.Search
                        },
                        contentDescription = null,
                    )
                }
            }
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = viewState.isRefreshing,
            onRefresh = { intentDispatcher(BooksListIntent.OnRefresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                headerContent?.invoke()

                AnimatedVisibility(
                    visible = viewState.isSearchVisible,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut(),
                ) {
                    BookSearchBar(
                        searchFieldState = searchFieldState,
                        isVisible = viewState.isSearchVisible,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                LibrarySectionRow(
                    viewState = viewState,
                    onSectionSelected = { section ->
                        intentDispatcher(BooksListIntent.OnSectionSelected(section))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                BookFilterChipsRow(
                    activeFilters = viewState.filterState.activeQuickFilters,
                    onFilterToggle = { filter ->
                        intentDispatcher(BooksListIntent.OnQuickFilterToggled(filter))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                )

                BookSortSelector(
                    sortConfig = viewState.sortConfig,
                    onSortChanged = { sortConfig ->
                        intentDispatcher(BooksListIntent.OnSortChanged(sortConfig))
                    },
                    modifier = Modifier.fillMaxWidth(),
                )

                ViewModeSelector(
                    viewMode = viewState.viewMode,
                    onViewModeChanged = { viewMode ->
                        intentDispatcher(BooksListIntent.OnViewModeChanged(viewMode))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                )

                if (viewState.filteredBooks.isEmpty() && !viewState.isLoading) {
                    EmptyBooksState(
                        hasActiveFilters = viewState.filterState.hasActiveFilters || viewState.searchQuery.isNotBlank(),
                        onResetFilters = {
                            intentDispatcher(BooksListIntent.OnClearAllFilters)
                            searchFieldState.edit { delete(0, length) }
                        },
                        modifier = Modifier.fillMaxSize(),
                    )
                } else if (viewState.viewMode == BookListViewMode.GRID) {
                    BooksGrid(
                        viewState = viewState,
                        intentDispatcher = intentDispatcher,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        items(
                            items = viewState.filteredBooks,
                            key = { it.uuid },
                        ) { book ->
                            BookItemCard(
                                modifier = Modifier.animateItem(),
                                book = book,
                                isFavorite = book.uuid in viewState.favoriteBookUuids,
                                onClick = {
                                    intentDispatcher(BooksListIntent.OnBookClicked(book))
                                },
                                onFavoriteClick = {
                                    intentDispatcher(BooksListIntent.OnFavoriteClicked(book.uuid))
                                },
                                progressInfo = viewState.bookProgressInfo[book.uuid],
                                subtitleContent = {
                                    if (book.series.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        val seriesInfo = book.series.first()
                                        val seriesText = if (seriesInfo.position != null) {
                                            stringResource(
                                                StringRes.books_series_with_position,
                                                seriesInfo.name,
                                                seriesInfo.position,
                                            )
                                        } else {
                                            seriesInfo.name
                                        }
                                        Text(
                                            text = seriesText,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                        )
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BooksGrid(
    viewState: BooksListViewState,
    intentDispatcher: IntentDispatcher<BooksListIntent>,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 128.dp),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (viewState.isLoading) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
        gridItems(
            items = viewState.filteredBooks,
            key = { it.uuid },
        ) { book ->
            BookGridCard(
                modifier = Modifier.animateItem(),
                book = book,
                isFavorite = book.uuid in viewState.favoriteBookUuids,
                onClick = {
                    intentDispatcher(BooksListIntent.OnBookClicked(book))
                },
                onFavoriteClick = {
                    intentDispatcher(BooksListIntent.OnFavoriteClicked(book.uuid))
                },
                progressInfo = viewState.bookProgressInfo[book.uuid],
            )
        }
    }
}

@Composable
private fun LibrarySectionRow(
    viewState: BooksListViewState,
    onSectionSelected: (BookLibrarySection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BookLibrarySection.entries.forEach { section ->
            val count = viewState.sectionCount(section)
            FilterChip(
                selected = viewState.selectedSection == section,
                onClick = { onSectionSelected(section) },
                label = { Text("${section.toDisplayLabel()} $count") },
            )
        }
    }
}

@Composable
private fun ViewModeSelector(
    viewMode: BookListViewMode,
    onViewModeChanged: (BookListViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${viewMode.toDisplayLabel()} view",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        BookListViewMode.entries.forEach { mode ->
            FilterChip(
                selected = viewMode == mode,
                onClick = { onViewModeChanged(mode) },
                label = { Text(mode.toDisplayLabel()) },
            )
        }
    }
}

private fun BookLibrarySection.toDisplayLabel(): String = when (this) {
    BookLibrarySection.ALL -> "All"
    BookLibrarySection.IN_PROGRESS -> "Reading"
    BookLibrarySection.DOWNLOADED -> "Downloaded"
    BookLibrarySection.FAVORITES -> "Favorites"
    BookLibrarySection.READ_ALOUD -> "Read Aloud"
    BookLibrarySection.LOCAL -> "Local"
}

private fun BookListViewMode.toDisplayLabel(): String = when (this) {
    BookListViewMode.LIST -> "List"
    BookListViewMode.GRID -> "Grid"
}

@Composable
private fun EmptyBooksState(
    hasActiveFilters: Boolean,
    onResetFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    if (hasActiveFilters) {
                        StringRes.books_empty_filtered_title
                    } else {
                        StringRes.books_empty_title
                    }
                ),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = stringResource(
                    if (hasActiveFilters) {
                        StringRes.books_empty_filtered_subtitle
                    } else {
                        StringRes.books_empty_subtitle
                    }
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )

            if (hasActiveFilters) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = onResetFilters) {
                    Text(text = stringResource(StringRes.books_reset_filters))
                }
            }
        }
    }
}

@Composable
private fun ImportingDialog(
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        onDismissRequest = { /* Non-dismissible while importing */ },
        modifier = modifier,
        confirmButton = { /* No buttons - auto-dismisses when done */ },
        title = {
            Text(
                text = stringResource(StringRes.books_importing),
                style = MaterialTheme.typography.titleMedium,
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        },
    )
}
