package com.retro99.base.ui.di

import com.retro99.base.ui.sharing.FileSharer
import com.retro99.base.ui.sharing.WebFileSharer
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformBaseUiModule {

    @Single
    fun provideFileSharer(): FileSharer = WebFileSharer()
}
