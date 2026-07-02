package com.retro99.reader.ui.playback

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class JsNowPlayingProvider : NowPlayingProvider {
    override val nowPlayingInfo: StateFlow<NowPlayingInfo?> = MutableStateFlow(null)
    override val isPlaying: StateFlow<Boolean> = MutableStateFlow(false)

    override fun togglePlayPause() {
    }

    override fun stop() {
    }
}
