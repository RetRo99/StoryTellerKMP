package com.retro99.base.buildconfig

import org.koin.core.annotation.Single
import platform.Foundation.NSBundle
import kotlin.experimental.ExperimentalNativeApi

/**
 * iOS implementation of BuildConfig.
 * Uses Platform.isDebugBinary to determine if the app is running in debug mode.
 * Uses NSBundle to get version information.
 */
@Single(binds = [BuildConfig::class])
class BuildConfigIos : BuildConfig {
    @OptIn(ExperimentalNativeApi::class)
    override val isDebug: Boolean
        get() = Platform.isDebugBinary

    override val versionName: String
        get() = NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleShortVersionString")
                as? String ?: "Unknown"

    override val versionCode: Int
        get() = (NSBundle.mainBundle.objectForInfoDictionaryKey("CFBundleVersion")
                as? String)?.toIntOrNull() ?: 0
}