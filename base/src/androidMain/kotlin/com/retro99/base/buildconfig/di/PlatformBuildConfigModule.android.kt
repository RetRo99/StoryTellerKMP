package com.retro99.base.buildconfig.di

import android.content.Context
import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.BuildConfigAndroid
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android implementation of platform-specific BuildConfig module.
 * Registers BuildConfigAndroid as the BuildConfig implementation.
 */
actual val platformBuildConfigModule: Module = module {
    single<BuildConfig> { BuildConfigAndroid(get<Context>()) }
}

