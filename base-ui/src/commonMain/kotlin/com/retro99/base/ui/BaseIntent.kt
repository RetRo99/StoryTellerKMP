package com.retro99.base.ui

import kotlin.jvm.JvmInline

interface BaseIntent

@JvmInline
value class IntentDispatcher<T : BaseIntent>(private val handler: (T) -> Unit) {
    operator fun invoke(intent: T) {
        handler(intent)
    }
}
