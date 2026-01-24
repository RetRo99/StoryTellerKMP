package com.retro99.base.buildconfig

interface BuildConfig {
    val isDebug: Boolean
}

expect fun getBuildConfig(): BuildConfig