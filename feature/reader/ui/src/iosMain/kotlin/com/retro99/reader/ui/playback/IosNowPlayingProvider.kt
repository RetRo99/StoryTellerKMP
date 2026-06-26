package com.retro99.reader.ui.playback

import com.retro99.reader.ui.bridge.EpubReaderBridgeRegistry
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.koin.core.annotation.Single

@Single(binds = [NowPlayingProvider::class])
class IosNowPlayingProvider : NowPlayingProvider {

    private val _nowPlayingInfo = MutableStateFlow<NowPlayingInfo?>(null)
    private val _isPlaying = MutableStateFlow(false)

    override val nowPlayingInfo: StateFlow<NowPlayingInfo?> = _nowPlayingInfo.asStateFlow()

    override val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val bridge get() = EpubReaderBridgeRegistry.getBridge()

    fun updateNowPlayingInfo(info: NowPlayingInfo?) {
        _nowPlayingInfo.value = info
    }

    fun updateIsPlaying(playing: Boolean) {
        _isPlaying.value = playing
    }

    override fun togglePlayPause() {
        if (_isPlaying.value) {
            bridge?.pauseAudio()
        } else {
            bridge?.resumeAudio()
        }
    }

    override fun stop() {
        bridge?.pauseAudio()
        _nowPlayingInfo.value = null
        _isPlaying.value = false
    }
}
