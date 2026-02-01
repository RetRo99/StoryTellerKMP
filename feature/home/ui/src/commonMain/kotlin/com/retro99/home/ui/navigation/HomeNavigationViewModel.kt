package com.retro99.home.ui.navigation

import com.retro99.base.ui.BaseViewModel
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class HomeNavigationViewModel : BaseViewModel<HomeNavigationState, HomeNavigationIntent>(
    HomeNavigationState(),
) {

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

