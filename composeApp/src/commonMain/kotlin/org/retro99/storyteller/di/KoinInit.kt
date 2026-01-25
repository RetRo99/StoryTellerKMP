package org.retro99.storyteller.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Platform-specific Koin modules including the generated AppModule.
 * Each platform provides the AppModule().module along with platform-specific bindings.
 */
expect fun platformModules(): List<Module>

/**
 * Platform-specific Kotzilla analytics setup.
 * Android and iOS call analytics(), JVM does nothing.
 */
expect fun KoinApplication.setupAnalytics()

/**
 * Initialize Koin with all application modules.
 */
fun initKoin(additionalModules: List<Module> = emptyList()): KoinApplication {
    return startKoin {
        modules(
            platformModules() + additionalModules
        )
        setupAnalytics()
    }
}

