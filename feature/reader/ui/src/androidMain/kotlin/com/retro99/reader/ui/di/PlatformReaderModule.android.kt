package com.retro99.reader.ui.di

import android.content.Context
import com.retro99.reader.ui.controller.AndroidEpubReaderController
import com.retro99.reader.ui.controller.EpubReaderController
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Android implementation of platform-specific Reader module.
 * Registers AndroidEpubReaderController as the EpubReaderController implementation.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformReaderModule {

    @Single
    fun providesEpubReaderController(context: Context): EpubReaderController {
        return AndroidEpubReaderController(context)
    }
}

