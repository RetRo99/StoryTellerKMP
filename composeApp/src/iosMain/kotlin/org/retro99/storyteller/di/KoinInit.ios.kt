package org.retro99.storyteller.di

import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.BuildConfigIos
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModules(): List<Module> = listOf(
    module {
        single<BuildConfig> { BuildConfigIos() }
    }
)

