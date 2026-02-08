package com.retro99.reader.ui.di

import com.retro99.reader.ui.bridge.EpubReaderBridge
import com.retro99.reader.ui.bridge.EpubReaderBridgeRegistry
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * iOS implementation of platform-specific Reader module.
 * Registers IosEpubPublicationService as the EpubPublicationService implementation.
 */
@Module
actual class PlatformReaderModule {

    @Single
    fun providesEpubReaderBridge(): EpubReaderBridge {
        return EpubReaderBridgeRegistry.getBridge() ?: error("EpubReaderBridge not registered")
    }
}
