package org.retro99.storyteller.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module

/**
 * Platform-specific Koin modules.
 * Android provides Context, iOS/JVM provide empty list.
 */
expect fun platformModules(): List<Module>

/**
 * Initialize Koin with all application modules.
 */
fun initKoin(additionalModules: List<Module> = emptyList()): KoinApplication {
    return startKoin {
        modules(
            platformModules() + additionalModules
        )
    }
}

