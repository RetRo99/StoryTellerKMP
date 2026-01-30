package com.retro99.reader.ui.di

import com.retro99.reader.ui.controller.EpubReaderController
import com.retro99.reader.ui.controller.IosEpubReaderController
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * iOS implementation of platform-specific Reader module.
 * Registers IosEpubReaderController as the EpubReaderController implementation.
 */
@Module
actual class PlatformReaderModule {

    @Single
    fun providesEpubReaderController(): EpubReaderController {
        return IosEpubReaderController()
    }
}

