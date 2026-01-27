package com.retro99.analytics.api

interface Analytics {
    fun logException(throwable: Throwable, message: String?)
}