package com.retro99.base.ui.di

import android.content.Context
import com.retro99.base.ui.sharing.AndroidFileSharer
import com.retro99.base.ui.sharing.FileSharer
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Android implementation of platform-specific BaseUi module.
 * Registers AndroidFileSharer as the FileSharer implementation.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformBaseUiModule {

    @Single
    fun provideFileSharer(context: Context): FileSharer {
        return AndroidFileSharer(context)
    }
}

