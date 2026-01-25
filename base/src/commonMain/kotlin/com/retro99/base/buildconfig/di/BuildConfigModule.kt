package com.retro99.base.buildconfig.di

import com.retro99.base.buildconfig.BuildConfig
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module
import org.koin.core.annotation.Named
import org.koin.core.annotation.Single

@Module
@ComponentScan("com.retro99.base.buildconfig")
class BuildConfigModule {

    @Single
    @Named("isDebug")
    fun provideIsDebug(buildConfig: BuildConfig): Boolean = buildConfig.isDebug
}