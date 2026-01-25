package org.retro99.storyteller.di

import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.ksp.generated.*

actual fun KoinApplication.setupAnalytics() {
    analytics()
}

actual fun initKoin(
    additionalModules: List<Module>,
    platformConfiguration: KoinApplication.() -> Unit
): KoinApplication {
    return StoryTellerKoinApp().startKoin {
        platformConfiguration()
        modules(additionalModules)
        setupAnalytics()
    }
}
