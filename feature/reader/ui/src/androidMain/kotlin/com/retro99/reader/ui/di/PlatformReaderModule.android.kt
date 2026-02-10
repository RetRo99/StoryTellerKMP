package com.retro99.reader.ui.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import org.koin.core.annotation.Module
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

/** Seek increment in milliseconds (10 seconds) */
private const val SEEK_INCREMENT_MS = 10_000L

/**
 * Android implementation of platform-specific Reader module.
 * Registers AndroidEpubPublicationService as the EpubPublicationService implementation.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformReaderModule {

    @androidx.annotation.OptIn(UnstableApi::class)
    @Scope(ReaderScope::class)
    @Scoped
    fun provideExoPlayer(context: Context): ExoPlayer {
        return ExoPlayer.Builder(context)
            .setSeekBackIncrementMs(SEEK_INCREMENT_MS)
            .setSeekForwardIncrementMs(SEEK_INCREMENT_MS)
            .setHandleAudioBecomingNoisy(true)
            .build()
    }
}

