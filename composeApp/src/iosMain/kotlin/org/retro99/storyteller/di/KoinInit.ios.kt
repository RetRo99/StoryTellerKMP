package org.retro99.storyteller.di

import com.retro99.base.buildconfig.di.platformBuildConfigModule
import com.retro99.preferences.implementation.di.platformPreferencesModule
import io.kotzilla.sdk.analytics.koin.analytics
import org.koin.core.KoinApplication
import org.koin.core.module.Module
import org.koin.plugin.module.dsl.startKoin

actual fun initKoin(
    additionalModules: List<Module>,
    platformConfiguration: KoinApplication.() -> Unit,
): KoinApplication {
    return startKoin<StoryTellerKoinApp> {
        platformConfiguration()
        modules(additionalModules + platformBuildConfigModule + platformPreferencesModule)
        analytics()
    }
}
