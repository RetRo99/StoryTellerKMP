package com.retro99.preferences.api

interface Preferences {
    fun getStringOrNull(key: PreferencesKey): String?
    fun putString(key: PreferencesKey, value: String)
    fun remove(key: PreferencesKey)
}

enum class PreferencesKey {
    AccessToken,
    Username,
}