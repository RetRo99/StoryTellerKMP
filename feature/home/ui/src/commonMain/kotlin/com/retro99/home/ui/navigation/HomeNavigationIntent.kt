package com.retro99.home.ui.navigation

import com.retro99.base.ui.BaseIntent
import com.retro99.books.domain.model.BookType

/**
 * Intents for HomeNavigationViewModel.
 *
 * All navigation actions go through the ViewModel to maintain the intent dispatcher pattern.
 * The ViewModel emits [HomeNavigationEvent]s which are consumed by the composable
 * to update the navigation state.
 */
sealed interface HomeNavigationIntent : BaseIntent {
    // UI state intents
    data class UpdateBubblePosition(val side: BubbleSide, val yFraction: Float) : HomeNavigationIntent
    data object RefreshCurrentlyReading : HomeNavigationIntent

    // Navigation intents
    data class NavigateTo(val destination: HomeDestination) : HomeNavigationIntent
    data class SwitchTab(val tab: HomeTab) : HomeNavigationIntent
    data object GoBack : HomeNavigationIntent

    /**
     * Navigate to reader from the continue reading bubble.
     */
    data class OpenReader(val bookUuid: String, val bookType: BookType) : HomeNavigationIntent
}

