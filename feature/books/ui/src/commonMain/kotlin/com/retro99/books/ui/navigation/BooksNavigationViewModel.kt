package com.retro99.books.ui.navigation

import com.retro99.base.ui.BaseViewModel
import org.koin.core.annotation.KoinViewModel

@KoinViewModel
class BooksNavigationViewModel : BaseViewModel<BooksNavigationState, BooksNavigationIntent>() {

    override val initialState = BooksNavigationState()

    override fun onIntent(intent: BooksNavigationIntent) {
        when (intent) {
            BooksNavigationIntent.OnBackClicked -> {
                updateState { state ->
                    state.copy(backStack = state.backStack.dropLast(1))
                }
            }

            is BooksNavigationIntent.NavigateTo -> {
                updateState { state ->
                    state.copy(backStack = state.backStack + intent.destination)
                }
            }
        }
    }
}

