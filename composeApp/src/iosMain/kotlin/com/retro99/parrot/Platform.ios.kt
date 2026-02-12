package com.retro99.parrot

import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    // iOS devices don't have e-ink displays
    override val isEink: Boolean = false
}

actual fun getPlatform(): Platform = IOSPlatform()

