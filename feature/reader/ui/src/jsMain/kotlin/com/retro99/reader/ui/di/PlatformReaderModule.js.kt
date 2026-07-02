package com.retro99.reader.ui.di

import com.retro99.reader.ui.playback.JsNowPlayingProvider
import com.retro99.reader.ui.playback.NowPlayingProvider
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformReaderModule {

    @Single
    fun provideNowPlayingProvider(): NowPlayingProvider = JsNowPlayingProvider()
}
