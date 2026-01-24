package com.retro99.base.buildconfig.di

import com.retro99.base.annotations.Named
import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.getBuildConfig
import me.tatarka.inject.annotations.Provides
import software.amazon.lastmile.kotlin.inject.anvil.AppScope
import software.amazon.lastmile.kotlin.inject.anvil.ContributesTo
import software.amazon.lastmile.kotlin.inject.anvil.SingleIn

@ContributesTo(AppScope::class)
interface BuildConfigComponent {

    // TODO create a new component for ios/android
    @Provides
    @SingleIn(AppScope::class)
    fun provideBuildConfig(): BuildConfig = getBuildConfig()

    @Provides
    @SingleIn(AppScope::class)
    @Named("isDebug")
    fun provideIsDebug(buildConfig: BuildConfig): Boolean = buildConfig.isDebug
}