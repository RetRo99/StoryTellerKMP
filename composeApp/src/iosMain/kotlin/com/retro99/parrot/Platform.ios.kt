package com.retro99.parrot

import com.retro99.base.ui.platform.isEinkDisplay
import platform.UIKit.UIDevice

class IOSPlatform : Platform {
    override val name: String =
        UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion

    override val isEink: Boolean = isEinkDisplay()
}

actual fun getPlatform(): Platform = IOSPlatform()

