package com.retro99.base.buildconfig.di

import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.BuildConfigIos
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * iOS implementation of platform-specific BuildConfig module.
 * Registers BuildConfigIos as the BuildConfig implementation.
 */
@Module
actual class PlatformBuildConfigModule {

    @Single
    fun providesBuildConfig(): BuildConfig {
        return BuildConfigIos()
    }
}

