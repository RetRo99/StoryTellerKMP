package org.retro99.storyteller.di

import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.plugin.module.dsl.startKoin

/**
 * Initialize Koin with all application modules.
 *
 * Uses KSP-generated startKoin extension from @KoinApplication annotation
 * which automatically includes all @Configuration annotated modules.
 *
 * @param additionalModules Additional modules to include
 * @param platformConfiguration Lambda for platform-specific configuration (e.g., androidContext, androidLogger)
 */
fun initKoin(
    additionalModules: List<Module> = emptyList(),
    platformConfiguration: KoinApplication.() -> Unit = {},
): KoinApplication {
    return startKoin<StoryTellerKoinApp> {
        platformConfiguration()
        modules(additionalModules)
        analytics()
    }
}

