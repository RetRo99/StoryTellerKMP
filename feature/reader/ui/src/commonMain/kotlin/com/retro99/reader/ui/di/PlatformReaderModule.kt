package com.retro99.reader.ui.di

import org.koin.core.annotation.Module

/**
 * Platform-specific Koin module that provides the EpubReaderController implementation.
 *
 * Uses expect/actual pattern with @Module annotation so KSP can process
 * platform-specific implementations and include them in the module graph.
 *
 * Each platform implementation provides its own controller:
 * - Android: AndroidEpubReaderController (uses Readium)
 * - iOS: IosEpubReaderController (placeholder)
 */
@Module
expect class PlatformReaderModule()

