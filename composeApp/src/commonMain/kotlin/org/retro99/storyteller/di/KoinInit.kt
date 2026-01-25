package org.retro99.storyteller.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Platform-specific Koin modules.
 * Each platform provides platform-specific bindings (e.g., BuildConfig).
 */
expect fun platformModules(): List<Module>

/**
 * Platform-specific app modules from KSP-generated code.
 * KSP generates the .module extension per-platform, so this must be actual.
 */
expect fun appModules(): List<Module>

/**
 * Platform-specific Kotzilla analytics setup.
 * Android and iOS call analytics().
 */
expect fun KoinApplication.setupAnalytics()

/**
 * Initialize Koin with all application modules.
 */
fun initKoin(additionalModules: List<Module> = emptyList()): KoinApplication {
    return startKoin {
        modules(
            platformModules() + appModules() + additionalModules
        )
        setupAnalytics()
    }
}

