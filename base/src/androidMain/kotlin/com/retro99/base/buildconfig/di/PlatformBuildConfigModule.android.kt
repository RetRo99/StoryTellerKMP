package com.retro99.base.buildconfig.di

import android.content.Context
import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.BuildConfigAndroid
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Android implementation of platform-specific BuildConfig module.
 * Registers BuildConfigAndroid as the BuildConfig implementation.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformBuildConfigModule {

    @Single
    fun providesBuildConfig(context: Context): BuildConfig {
        return BuildConfigAndroid(context)
    }
}

