package com.retro99.base

actual fun formatCurrentTime(): String =
    js("new Date().toLocaleTimeString()")
