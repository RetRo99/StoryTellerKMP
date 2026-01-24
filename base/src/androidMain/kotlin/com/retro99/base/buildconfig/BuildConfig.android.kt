package com.retro99.base.buildconfig

class BuildConfigAndroid: BuildConfig {
    override val isDebug: Boolean
        get() = com.retro99.base.BuildConfig.DEBUG
}
actual fun getBuildConfig(): BuildConfig = BuildConfigAndroid()