package com.retro99.reader.ui.di

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.retro99.reader.ui.publication.EpubPublication
import org.koin.core.annotation.Module
import org.koin.core.annotation.Scope
import org.koin.core.annotation.Scoped

/** Seek increment in milliseconds (10 seconds) */
private const val SEEK_INCREMENT_MS = 10_000L

/**
 * Wrapper for initial audio position from saved reading progress.
 * Used to inject the initial seek bar position into LocatorTracker.
 */
@JvmInline
value class InitialAudioPosition(val positionMs: Long?)

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

    @Scope(ReaderScope::class)
    @Scoped
    fun provideInitialAudioPosition(publication: EpubPublication): InitialAudioPosition {
        return InitialAudioPosition(publication.initialPosition?.audioTimestampMs)
    }
}

