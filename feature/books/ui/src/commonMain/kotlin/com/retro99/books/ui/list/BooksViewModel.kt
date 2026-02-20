package com.retro99.books.ui.list

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.delete
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.BookAnalyticsEvent
import com.retro99.analytics.api.NavigationAnalyticsEvent
import com.retro99.base.result.log
import com.retro99.base.ui.BaseViewModel
import com.retro99.books.domain.usecase.GetBooksUseCase
import com.retro99.books.domain.usecase.ImportEpubUseCase
import com.retro99.books.domain.usecase.ObserveAllFavoritesUseCase
import com.retro99.books.domain.usecase.ToggleFavoriteUseCase
import com.retro99.books.ui.model.BookFilterState
import com.retro99.books.ui.model.BookListSettings
import com.retro99.books.ui.model.BookQuickFilter
import com.retro99.books.ui.model.BookSortConfig
import com.retro99.books.ui.model.BookUiModel
import com.retro99.books.ui.model.toUiModel
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.implementation.usecase.GetUserPreferenceUseCase
import com.retro99.preferences.implementation.usecase.SaveUserPreferenceUseCase
import com.retro99.reader.domain.usecase.GetAllBooksProgressInfoUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.koin.core.annotation.InjectedParam
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class BooksViewModel(
    @InjectedParam private val onNavigateToBookDetail: (book: BookUiModel) -> Unit,
    @Provided private val getBooksUseCase: GetBooksUseCase,
    @Provided private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    @Provided private val observeAllFavoritesUseCase: ObserveAllFavoritesUseCase,
    @Provided private val importEpubUseCase: ImportEpubUseCase,
    @Provided private val getAllBooksProgressInfoUseCase: GetAllBooksProgressInfoUseCase,
    @Provided private val analytics: Analytics,
    @Provided private val getUserPreferenceUseCase: GetUserPreferenceUseCase,
    @Provided private val saveUserPreferenceUseCase: SaveUserPreferenceUseCase,
) : BaseViewModel<BooksListViewState, BooksListIntent>(BooksListViewState()) {

    val searchFieldState = TextFieldState()

    init {
        loadFilterSortSettings()
        observeBooks()
        observeFavorites()
        observeSearchQuery()
    }

    override fun onIntent(intent: BooksListIntent) {
        when (intent) {
            BooksListIntent.OnRefresh -> refreshProgressInfo()
            BooksListIntent.OnSearchToggled -> toggleSearch()
            BooksListIntent.OnFilterToggled -> toggleFilter()
            is BooksListIntent.OnBookClicked -> onNavigateToBookDetail(intent.book)
            is BooksListIntent.OnFavoriteClicked -> toggleFavorite(intent.bookUuid)
            is BooksListIntent.OnImportBook -> importBook(intent.file)
            is BooksListIntent.OnQuickFilterToggled -> toggleQuickFilter(intent.filter)
            BooksListIntent.OnClearAllFilters -> clearAllFilters()
            is BooksListIntent.OnSortChanged -> updateSort(intent.sortConfig)
        }
    }

    /**
     * Refreshes the progress/cache info for all books.
     * Called on pull-to-refresh to update cache status after downloads.
     */
    private fun refreshProgressInfo() {
        viewModelScope.launch {
            updateState { it.copy(isRefreshing = true) }
            val bookUuids = viewState.value.books.map { it.uuid }
            if (bookUuids.isNotEmpty()) {
                val progressInfo = getAllBooksProgressInfoUseCase(bookUuids)
                    .mapValues { (_, info) -> info.toUiModel() }
                updateState {
                    it.copy(
                        bookProgressInfo = progressInfo,
                        isRefreshing = false,
                    )
                }
            } else {
                updateState { it.copy(isRefreshing = false) }
            }
        }
    }

    private fun toggleSearch() {
        val currentlyVisible = viewState.value.isSearchVisible
        // Only track when opening search, not closing
        if (!currentlyVisible) {
            analytics.logEvent(NavigationAnalyticsEvent.SearchOpened(source = "books_list"))
        }
        if (currentlyVisible) {
            // Clear search when hiding
            searchFieldState.edit { delete(0, length) }
        }
        updateState { it.copy(isSearchVisible = !currentlyVisible) }
    }

    private fun toggleFilter() {
        updateState { it.copy(isFilterVisible = !it.isFilterVisible) }
    }

    private fun toggleQuickFilter(filter: BookQuickFilter) {
        updateState { state ->
            val currentFilters = state.filterState.activeQuickFilters
            val newFilters = if (filter in currentFilters) {
                currentFilters - filter
            } else {
                currentFilters + filter
            }
            state.copy(filterState = state.filterState.copy(activeQuickFilters = newFilters))
        }
        saveFilterSortSettings()
    }

    private fun clearAllFilters() {
        updateState { it.copy(filterState = BookFilterState()) }
        saveFilterSortSettings()
    }

    private fun updateSort(sortConfig: BookSortConfig) {
        updateState { it.copy(sortConfig = sortConfig) }
        saveFilterSortSettings()
    }

    private fun loadFilterSortSettings() {
        val settings = getUserPreferenceUseCase<BookListSettings>(PreferencesKey.BookListFilterSort)
        if (settings != null) {
            updateState {
                it.copy(
                    filterState = settings.filterState,
                    sortConfig = settings.sortConfig,
                )
            }
        }
    }

    private fun saveFilterSortSettings() {
        val state = viewState.value
        val settings = BookListSettings(
            filterState = state.filterState,
            sortConfig = state.sortConfig,
        )
        saveUserPreferenceUseCase(PreferencesKey.BookListFilterSort, settings)
    }

    private fun observeSearchQuery() {
        snapshotFlow { searchFieldState.text.toString() }
            .onEach { query ->
                updateState { it.copy(searchQuery = query) }
            }
            .launchIn(viewModelScope)
    }

    private fun toggleFavorite(bookUuid: String) {
        val currentIsFavorite = viewState.value.favoriteBookUuids.contains(bookUuid)
        // Log the new state (opposite of current)
        analytics.logEvent(
            BookAnalyticsEvent.FavoriteToggled(
                bookUuid = bookUuid,
                isFavorite = !currentIsFavorite,
                source = "list",
            )
        )
        viewModelScope.launch {
            toggleFavoriteUseCase(bookUuid)
        }
    }

    private fun observeFavorites() {
        observeAllFavoritesUseCase()
            .onEach { favoriteUuids ->
                updateState {
                    it.copy(favoriteBookUuids = favoriteUuids)
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeBooks() {
        getBooksUseCase()
            .onStart {
                updateState { it.copy(isLoading = true, error = null) }
            }
            .onEach { result ->
                result
                    .onSuccess { books ->
                        val uiBooks = books.map { book -> book.toUiModel() }
                        val bookUuids = books.map { it.uuid }
                        val progressInfo = getAllBooksProgressInfoUseCase(bookUuids)
                            .mapValues { (_, info) -> info.toUiModel() }
                        updateState {
                            it.copy(
                                books = uiBooks,
                                bookProgressInfo = progressInfo,
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

    private fun importBook(file: io.github.vinceglb.filekit.core.PlatformFile) {
        viewModelScope.launch {
            updateState { it.copy(isImporting = true) }
            importEpubUseCase(file)
                .onSuccess { book ->
                    analytics.logEvent(BookAnalyticsEvent.BookImported(bookUuid = book.uuid))
                }
                .onFailure { error ->
                    error.log(analytics, "BooksViewModel: Failed to import book")
                }
            updateState { it.copy(isImporting = false) }
        }
    }
}

