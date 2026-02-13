package com.retro99.base.buildconfig

interface BuildConfig {
    val isDebug: Boolean
    val versionName: String
    val versionCode: Int
}