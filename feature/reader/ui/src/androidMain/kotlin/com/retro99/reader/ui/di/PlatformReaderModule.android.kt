package com.retro99.reader.ui.di

import org.koin.core.annotation.Module

/**
 * Android implementation of platform-specific Reader module.
 * Registers AndroidEpubPublicationService as the EpubPublicationService implementation.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformReaderModule

