package com.retro99.base.buildconfig.di

import com.retro99.base.buildconfig.BuildConfig
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module
actual class PlatformBuildConfigModule {

    @Single
    fun providesBuildConfig(): BuildConfig = object : BuildConfig {
        override val isDebug = false
        override val versionName = "1.0.0"
        override val versionCode = 1
    }
}
