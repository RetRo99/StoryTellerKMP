package org.retro99.storyteller.di

import com.retro99.base.buildconfig.BuildConfig
import com.retro99.base.buildconfig.BuildConfigJvm
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module

actual fun platformModules(): List<Module> = listOf(
    module {
        single<BuildConfig> { BuildConfigJvm() }
    }
)

/**
 * JVM doesn't support Kotzilla analytics, so this is a no-op.
 */
actual fun KoinApplication.setupAnalytics() {
    // No-op: Kotzilla SDK doesn't support JVM desktop
}

