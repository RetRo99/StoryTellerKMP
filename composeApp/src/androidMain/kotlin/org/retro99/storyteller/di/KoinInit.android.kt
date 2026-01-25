package org.retro99.storyteller.di

import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.ksp.generated.startKoin

actual fun KoinApplication.setupAnalytics() {
    analytics()
}

actual fun initKoin(additionalModules: List<Module>): KoinApplication {
    return StoryTellerKoinApp().startKoin {
        modules(additionalModules)
        setupAnalytics()
    }
}
