package com.retro99.base.buildconfig.di

import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.BuildConfigIos
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * iOS implementation of platform-specific BuildConfig module.
 * Registers BuildConfigIos as the BuildConfig implementation.
 */
actual val platformBuildConfigModule: Module = module {
    single<BuildConfig> { BuildConfigIos() }
}

