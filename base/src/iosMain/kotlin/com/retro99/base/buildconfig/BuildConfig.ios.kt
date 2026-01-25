package com.retro99.base.buildconfig

import org.koin.core.annotation.Single
import kotlin.experimental.ExperimentalNativeApi

/**
 * iOS implementation of BuildConfig.
 * Uses Platform.isDebugBinary to determine if the app is running in debug mode.
 */
@Single(binds = [BuildConfig::class])
class BuildConfigIos : BuildConfig {
    @OptIn(ExperimentalNativeApi::class)
    override val isDebug: Boolean
        get() = Platform.isDebugBinary
}