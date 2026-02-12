package com.retro99.home.ui.navigation

import androidx.lifecycle.viewModelScope
import com.retro99.analytics.api.Analytics
import com.retro99.analytics.api.NavigationAnalyticsEvent
import com.retro99.base.ui.BaseViewModel
import com.retro99.home.ui.deeplink.DeepLinkDestination
import com.retro99.home.ui.deeplink.DeepLinkHandler
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@KoinViewModel
class HomeNavigationViewModel(
    deepLinkHandler: DeepLinkHandler,
    @Provided private val analytics: Analytics,
) : BaseViewModel<HomeNavigationState, HomeNavigationIntent>(
    HomeNavigationState(),
) {

    init {
        observeDeepLinks(deepLinkHandler)
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
        }
    }

    private fun handleBackClicked() {
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

