package com.retro99.login.data.oauth

import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSString
import platform.Foundation.create
import platform.Foundation.stringByRemovingPercentEncoding

@OptIn(BetaInteropApi::class)
internal actual fun String.decodeUrlComponent(): String {
    return NSString.create(string = this).stringByRemovingPercentEncoding() ?: this
}
