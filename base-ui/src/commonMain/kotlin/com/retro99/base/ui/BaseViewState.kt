package com.retro99.base.ui

import com.retro99.base.result.AppError

sealed class BaseViewState<out T> {
    data object Loading : BaseViewState<Nothing>()

    data class Success<T>(val data: T) : BaseViewState<T>()

    data class Error(val error: AppError) : BaseViewState<Nothing>()
}