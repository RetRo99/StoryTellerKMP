package com.retro99.base.buildconfig

class BuildConfigJvm : BuildConfig {
    override val isDebug: Boolean
        get() = System.getProperty("debug")?.toBoolean() ?: true
}

actual fun getBuildConfig(): BuildConfig = BuildConfigJvm()

