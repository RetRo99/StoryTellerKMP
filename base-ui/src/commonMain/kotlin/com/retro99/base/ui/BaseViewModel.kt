package com.retro99.base.ui

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

abstract class BaseViewModel<State, Intent : BaseIntent>(
    initialState: State,
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val viewState: StateFlow<State> = _state.asStateFlow()

    abstract fun onIntent(intent: Intent)

    protected fun updateState(update: (State) -> State) {
        _state.update(update)
    }

    fun currentViewState(): State = viewState.value
}
