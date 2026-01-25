package com.retro99.base.buildconfig

import android.content.Context
import android.content.pm.ApplicationInfo
import org.koin.core.annotation.Single

/**
 * Android implementation of BuildConfig.
 * Uses ApplicationInfo.FLAG_DEBUGGABLE to determine if the app is running in debug mode.
 * This flag is set based on the build type (debug vs release), matching BuildConfig.DEBUG.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Single(binds = [BuildConfig::class])
class BuildConfigAndroid(
    private val context: Context
) : BuildConfig {
    override val isDebug: Boolean
        get() = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
}