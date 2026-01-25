package com.retro99.base.ui

import androidx.lifecycle.ViewModel
import com.retro99.base.result.AppError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<State, Intent : BaseIntent> : ViewModel() {

    protected abstract val initialState: State

    private val _state = MutableStateFlow<BaseViewState<State>>(BaseViewState.Loading)
    val viewState: StateFlow<BaseViewState<State>> = _state.asStateFlow()

    abstract fun onIntent(intent: Intent)

    protected fun setState(state: State) {
        _state.value = BaseViewState.Success(state)
    }

    @Suppress("UNCHECKED_CAST")
    protected fun updateState(update: (State) -> State) {
        val current = (_state.value as? BaseViewState.Success<State>)?.data ?: initialState
        _state.value = BaseViewState.Success(update(current))
    }

    protected fun setError(error: AppError) {
        _state.value = BaseViewState.Error(error)
    }

    protected fun setLoading() {
        _state.value = BaseViewState.Loading
    }
}
