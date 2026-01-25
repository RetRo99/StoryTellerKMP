package org.retro99.storyteller.di

import org.koin.core.KoinApplication
import org.koin.core.module.Module

/**
 * Initialize Koin with all application modules.
 *
 * Uses KSP-generated startKoin extension from @KoinApplication annotation
 * which automatically includes all @Configuration annotated modules.
 *
 * This is an expect function because the generated StoryTellerKoinApp.startKoin()
 * extension is platform-specific (KSP generates code per platform).
 *
 * @param additionalModules Additional modules to include
 * @param platformConfiguration Lambda for platform-specific configuration (e.g., androidContext, androidLogger)
 */
expect fun initKoin(
    additionalModules: List<Module> = emptyList(),
    platformConfiguration: KoinApplication.() -> Unit = {}
): KoinApplication

