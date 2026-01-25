package org.retro99.storyteller.di

import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.BuildConfigIos
import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModules(): List<Module> = listOf(
    module {
        single<BuildConfig> { BuildConfigIos() }
    }
)

actual fun KoinApplication.setupAnalytics() {
    analytics()
}

