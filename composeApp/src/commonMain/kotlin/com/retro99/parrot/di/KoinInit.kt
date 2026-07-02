package com.retro99.parrot.di

import com.retro99.base.AppInitializer
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.plugin.module.dsl.startKoin

fun initKoin(
    additionalModules: List<Module> = emptyList(),
    platformConfiguration: KoinApplication.() -> Unit = {},
): KoinApplication {
    return startKoin<ParrotKoinApp> {
        platformConfiguration()
        modules(additionalModules)
        configurePlatformAnalytics()
    }.also { koinApp ->
        koinApp.koin.getAll<AppInitializer>().forEach { it.initialize() }
    }
}

internal expect fun KoinApplication.configurePlatformAnalytics()
