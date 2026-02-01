package com.retro99.reader.ui.di

import com.retro99.reader.ui.service.EpubPublicationService
import com.retro99.reader.ui.service.IosEpubPublicationService
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * iOS implementation of platform-specific Reader module.
 * Registers IosEpubPublicationService as the EpubPublicationService implementation.
 */
@Module
actual class PlatformReaderModule {

    @Single
    fun providesEpubPublicationService(): EpubPublicationService {
        return IosEpubPublicationService()
    }
}

