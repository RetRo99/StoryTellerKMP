package com.retro99.login.data.oauth

import java.net.URLDecoder

internal actual fun String.decodeUrlComponent(): String {
    return URLDecoder.decode(this, "UTF-8")
}
