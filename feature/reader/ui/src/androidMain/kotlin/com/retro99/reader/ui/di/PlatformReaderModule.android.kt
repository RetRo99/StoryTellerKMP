package com.retro99.reader.ui.di

import org.koin.core.annotation.Module

/**
 * Android implementation of platform-specific Reader module.
 *
 * Note: ExoPlayer is now owned and created by MediaPlaybackService instead of
 * being provided here. This allows the service to survive ReaderScope destruction
 * and enables headless playback (Android Auto).
 *
 * InitialAudioPosition is declared in the ReaderScope by the ViewModel,
 * not provided here, since it comes from the saved position loaded at runtime.
 */
@Module
actual class PlatformReaderModule

