package com.retro99.reader.ui.di

import android.content.Context
import com.retro99.reader.ui.service.AndroidEpubPublicationService
import com.retro99.reader.ui.service.EpubPublicationService
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Android implementation of platform-specific Reader module.
 * Registers AndroidEpubPublicationService as the EpubPublicationService implementation.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformReaderModule {

    @Single
    fun providesEpubPublicationService(context: Context): EpubPublicationService {
        return AndroidEpubPublicationService(context)
    }
}

