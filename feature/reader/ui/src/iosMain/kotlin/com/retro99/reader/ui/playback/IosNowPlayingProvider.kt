package com.retro99.reader.ui.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.koin.core.annotation.Single

/**
 * iOS stub implementation of NowPlayingProvider.
 * Audio playback is not yet supported on iOS, so this returns empty/false states.
 */
@Single(binds = [NowPlayingProvider::class])
class IosNowPlayingProvider : NowPlayingProvider {

    override val nowPlayingInfo: StateFlow<NowPlayingInfo?> = MutableStateFlow(null)

    override val isPlaying: StateFlow<Boolean> = MutableStateFlow(false)

    override fun togglePlayPause() {
        // No-op on iOS
    }

    override fun stop() {
        // No-op on iOS
    }
}

