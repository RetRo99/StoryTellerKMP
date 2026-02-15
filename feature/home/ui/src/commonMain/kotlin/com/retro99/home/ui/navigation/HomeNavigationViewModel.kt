package com.retro99.home.ui.navigation

import androidx.lifecycle.viewModelScope
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.NavigationAnalyticsEvent
import com.retro99.base.ui.BaseViewModel
import com.retro99.home.ui.deeplink.DeepLinkDestination
import com.retro99.home.ui.deeplink.DeepLinkHandler
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import com.retro99.preferences.api.getObject
import com.retro99.preferences.api.putObject
import com.retro99.reader.domain.usecase.GetCurrentlyReadingUseCase
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class HomeNavigationViewModel(
    deepLinkHandler: DeepLinkHandler,
    @Provided private val analytics: Analytics,
    @Provided private val getCurrentlyReadingUseCase: GetCurrentlyReadingUseCase,
    @Provided private val preferences: Preferences,
) : BaseViewModel<HomeNavigationState, HomeNavigationIntent>(
    HomeNavigationState(),
) {

    init {
        observeDeepLinks(deepLinkHandler)
        loadCurrentlyReading()
        loadBubblePosition()
        checkOpenLastBookOnLaunch()
    }

    /**
     * Loads the currently reading book from preferences.
     * This is called on init and when returning from the reader.
     */
    private fun loadCurrentlyReading() {
        val currentlyReading = getCurrentlyReadingUseCase()?.toUiModel()
        updateState { it.copy(currentlyReading = currentlyReading) }
    }

    /**
     * Checks if the "Open Last Book on Launch" setting is enabled and
     * automatically navigates to the reader if there's a currently reading book.
     */
    private fun checkOpenLastBookOnLaunch() {
        val isEnabled = preferences.getBoolean(
            PreferencesKey.OpenLastBookOnLaunch,
            defaultValue = false,
        )
        if (!isEnabled) return

        val currentlyReading = getCurrentlyReadingUseCase() ?: return

        // Navigate to the reader with the currently reading book
        updateState { state ->
            val booksTab = HomeTab.Books
            val currentBooksStack = state.backStacks[booksTab]
                ?: listOf(booksTab.startDestination)
            val newBooksStack = currentBooksStack
                .filterNot { it is HomeDestination.Reader }
                .plus(HomeDestination.Reader(currentlyReading.bookUuid, currentlyReading.bookType))
            state.copy(
                currentTab = booksTab,
                backStacks = state.backStacks + (booksTab to newBooksStack),
            )
        }
    }

    /**
     * Loads the bubble position from preferences.
     */
    private fun loadBubblePosition() {
        val bubblePosition = preferences.getObject<BubblePositionModel>(PreferencesKey.BubblePosition)
            ?: BubblePositionModel.DEFAULT
        updateState { it.copy(bubblePosition = bubblePosition) }
    }

    /**
     * Saves the bubble position to preferences.
     */
    private fun saveBubblePosition(side: BubbleSide, yFraction: Float) {
        val position = BubblePositionModel.fromBubbleSide(side, yFraction)
        preferences.putObject(PreferencesKey.BubblePosition, position)
        updateState { it.copy(bubblePosition = position) }
    }

    private fun observeDeepLinks(deepLinkHandler: DeepLinkHandler) {
        deepLinkHandler.navigationEvents
            .onEach { destination ->
                handleDeepLinkDestination(destination)
            }
            .launchIn(viewModelScope)
    }

    private fun handleDeepLinkDestination(destination: DeepLinkDestination) {
        when (destination) {
            is DeepLinkDestination.Reader -> {
                // Navigate to reader in the Books tab, replacing any existing reader
                // This ensures we don't stack multiple reader screens
                updateState { state ->
                    val booksTab = HomeTab.Books
                    val currentBooksStack = state.backStacks[booksTab]
                        ?: listOf(booksTab.startDestination)
                    val newBooksStack = currentBooksStack
                        .filterNot { it is HomeDestination.Reader }
                        .plus(HomeDestination.Reader(destination.bookUuid, destination.bookType))
                    state.copy(
                        currentTab = booksTab,
                        backStacks = state.backStacks + (booksTab to newBooksStack),
                    )
                }
            }
        }
    }

    override fun onIntent(intent: HomeNavigationIntent) {
        when (intent) {
            HomeNavigationIntent.OnBackClicked -> handleBackClicked()
            is HomeNavigationIntent.NavigateTo -> handleNavigateTo(intent.destination)
            is HomeNavigationIntent.SwitchTab -> handleSwitchTab(intent.tab)
            is HomeNavigationIntent.UpdateBubblePosition -> saveBubblePosition(intent.side, intent.yFraction)
        }
    }

    private fun handleBackClicked() {
        val currentState = viewState.value
        val wasInReader = currentState.currentBackStack.lastOrNull() is HomeDestination.Reader

        updateState { state ->
            val currentStack = state.currentBackStack
            if (currentStack.size > 1) {
                // Pop the current tab's back stack
                val newStack = currentStack.dropLast(1)
                state.copy(
                    backStacks = state.backStacks + (state.currentTab to newStack),
                )
            } else if (state.currentTab != HomeTab.DEFAULT) {
                // If at the root of a non-default tab, switch to the default tab
                state.copy(currentTab = HomeTab.DEFAULT)
            } else {
                // At the root of the default tab, do nothing (or let the system handle it)
                state
            }
        }

        // Refresh currently reading state when leaving the reader
        if (wasInReader) {
            loadCurrentlyReading()
        }
    }

    private fun handleNavigateTo(destination: HomeDestination) {
        updateState { state ->
            val currentStack = state.currentBackStack
            val newStack = currentStack + destination
            state.copy(
                backStacks = state.backStacks + (state.currentTab to newStack),
            )
        }
    }

    private fun handleSwitchTab(tab: HomeTab) {
        analytics.logEvent(NavigationAnalyticsEvent.TabSwitched(tabName = tab.name.lowercase()))
        updateState { state ->
            state.copy(currentTab = tab)
        }
    }
}

