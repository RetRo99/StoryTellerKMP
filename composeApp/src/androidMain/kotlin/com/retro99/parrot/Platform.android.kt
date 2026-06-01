package com.retro99.parrot

import android.os.Build
import com.retro99.base.ui.platform.isEinkDisplay

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"

    override val isEink: Boolean = isEinkDisplay()
}

actual fun getPlatform(): Platform = AndroidPlatform()

