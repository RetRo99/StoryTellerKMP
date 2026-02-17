package com.retro99.books.ui.list

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import com.retro99.base.ui.BaseScreen
import com.retro99.base.ui.IntentDispatcher
import com.retro99.books.ui.components.BookItemCard
import com.retro99.books.ui.components.BookSearchBar
import com.retro99.books.ui.model.BookUiModel
import com.retro99.translations.StringRes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import resources.translations.books_series_with_position

@Composable
fun BooksListScreen(
    onNavigateToBookDetail: (book: BookUiModel) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BooksViewModel = koinViewModel { parametersOf(onNavigateToBookDetail) },
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
) {
    val scope = rememberCoroutineScope()
    val filePickerLauncher = rememberFilePickerLauncher(
        type = PickerType.File(extensions = listOf("epub")),
        mode = PickerMode.Single,
    ) { file ->
        file?.let {
            scope.launch {
                val bytes = it.readBytes()
                intentDispatcher(BooksListIntent.OnImportBook(bytes, it.name))
            }
        }
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = viewState.filteredBooks,
                        key = { it.uuid },
                    ) { book ->
                        BookItemCard(
                            book = book,
                            isFavorite = book.uuid in viewState.favoriteBookUuids,
                            onClick = {
                                intentDispatcher(BooksListIntent.OnBookClicked(book))
                            },
                            onFavoriteClick = {
                                intentDispatcher(BooksListIntent.OnFavoriteClicked(book.uuid))
                            },
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
