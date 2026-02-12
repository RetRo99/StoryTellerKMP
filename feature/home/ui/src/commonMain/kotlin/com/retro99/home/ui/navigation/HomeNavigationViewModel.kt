package com.retro99.home.ui.navigation

import androidx.lifecycle.viewModelScope
import com.retro99.base.ui.BaseViewModel
import com.retro99.home.ui.deeplink.DeepLinkDestination
import com.retro99.home.ui.deeplink.DeepLinkHandler
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class HomeNavigationViewModel(
    deepLinkHandler: DeepLinkHandler,
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
                // Navigate to reader, replacing any existing reader in the stack
                // This ensures we don't stack multiple reader screens
                updateState { state ->
                    val newBackStack = state.backStack
                        .filterNot { it is HomeDestination.Reader }
                        .plus(HomeDestination.Reader(destination.bookUuid, destination.bookType))
                    state.copy(backStack = newBackStack)
                }
            }
        }
    }

    override fun onIntent(intent: HomeNavigationIntent) {
        when (intent) {
            HomeNavigationIntent.OnBackClicked -> {
                updateState { state ->
                    state.copy(backStack = state.backStack.dropLast(1))
                }
            }

            is HomeNavigationIntent.NavigateTo -> {
                updateState { state ->
                    state.copy(backStack = state.backStack + intent.destination)
                }
            }
        }
    }
}

