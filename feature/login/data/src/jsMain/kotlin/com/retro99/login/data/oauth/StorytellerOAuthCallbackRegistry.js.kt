package com.retro99.login.data.oauth

internal actual fun String.decodeUrlComponent(): String =
    js("decodeURIComponent(this)")
