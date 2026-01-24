package com.retro99.base.buildconfig.di

import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.getBuildConfig
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
class BuildConfigModule {

    // TODO create a new component for ios/android
    @Single
    fun provideBuildConfig(): BuildConfig = getBuildConfig()

    @Single
    @Named("isDebug")
    fun provideIsDebug(buildConfig: BuildConfig): Boolean = buildConfig.isDebug
}