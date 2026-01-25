package org.retro99.storyteller.di

import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.BuildConfigAndroid
import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module
import org.koin.ksp.generated.module

actual fun platformModules(): List<Module> = listOf(
    module {
        single<BuildConfig> { BuildConfigAndroid(get()) }
    },
    AppModule().module
)

actual fun KoinApplication.setupAnalytics() {
    analytics()
}

