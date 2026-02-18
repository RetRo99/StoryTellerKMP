package com.retro99.home.ui.navigation

import com.retro99.books.domain.model.BookType

/**
 * UI state for the Home screen (non-navigation concerns).
 *
 * Navigation state is now managed by [HomeNavigationStateHolder] using Nav3's
 * [rememberNavBackStack] for automatic persistence across process death.
 *
 * @property currentlyReading The book that the user was last reading for at least 1 minute.
 * @property bubblePosition The persisted position of the continue reading bubble. Null until loaded.
 */
data class HomeUiState(
    val currentlyReading: CurrentlyReadingUiModel? = null,
    val bubblePosition: BubblePositionModel? = null,
)

/**
 * Navigation events that the ViewModel emits to request navigation changes.
 * These are consumed by the composable which owns the navigation state.
 */
sealed interface HomeNavigationEvent {
    /**
     * Navigate to a destination within the current tab.
     */
    data class NavigateTo(val destination: HomeDestination) : HomeNavigationEvent

    /**
     * Switch to a different tab.
     */
    data class SwitchTab(val tab: HomeTab) : HomeNavigationEvent

    /**
     * Go back in the navigation stack.
     */
    data object GoBack : HomeNavigationEvent

    /**
     * Navigate to the reader screen, replacing any existing reader in the stack.
     * Used for deep links and "open last book on launch".
     */
    data class NavigateToReaderReplacing(
        val serverId: String,
        val bookUuid: String,
        val bookType: BookType,
        val tab: HomeTab = HomeTab.Books,
    ) : HomeNavigationEvent
}



