package com.retro99.home.ui.navigation

import com.retro99.base.ui.BaseViewModel
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class HomeNavigationViewModel : BaseViewModel<HomeNavigationState, HomeNavigationIntent>() {

    override val initialState = HomeNavigationState()

    init {
        setState(initialState)
    }

    override fun onIntent(intent: HomeNavigationIntent) {
        when (intent) {
            HomeNavigationIntent.OnLogout -> {
                // Logout is handled by parent navigation
            }

            HomeNavigationIntent.OnBackClicked -> {
                updateState { state ->
                    state.copy(backStack = state.backStack.dropLast(1))
                }
            }
        }
    }
}

