package com.retro99.base.buildconfig

import org.koin.core.annotation.Single

/**
 * JVM implementation of BuildConfig.
 * Uses system property "debug" to determine if the app is running in debug mode.
 * Defaults to true if the property is not set.
 */
@Single(binds = [BuildConfig::class])
class BuildConfigJvm : BuildConfig {
    override val isDebug: Boolean
        get() = System.getProperty("debug")?.toBoolean() ?: true
}
