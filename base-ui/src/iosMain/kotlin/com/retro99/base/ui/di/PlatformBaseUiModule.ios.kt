package com.retro99.base.ui.di

import com.retro99.base.ui.sharing.FileSharer
import com.retro99.base.ui.sharing.IosFileSharer
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * iOS implementation of platform-specific BaseUi module.
 * Registers IosFileSharer as the FileSharer implementation.
 */
@Module
actual class PlatformBaseUiModule {

    @Single
    fun provideFileSharer(): FileSharer {
        return IosFileSharer()
    }
}

