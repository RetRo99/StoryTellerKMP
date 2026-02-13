package com.retro99.preferences.api

import kotlinx.serialization.json.Json

interface Preferences {
    fun getStringOrNull(key: PreferencesKey): String?
    fun putString(key: PreferencesKey, value: String)
    fun getBoolean(key: PreferencesKey, defaultValue: Boolean = false): Boolean
    fun putBoolean(key: PreferencesKey, value: Boolean)
    fun getLong(key: PreferencesKey, defaultValue: Long = 0L): Long
    fun putLong(key: PreferencesKey, value: Long)
    fun remove(key: PreferencesKey)
}

inline fun <reified T> Preferences.putObject(key: PreferencesKey, value: T) {
    val jsonString = Json.encodeToString(value)
    putString(key, jsonString)
}

inline fun <reified T> Preferences.getObject(key: PreferencesKey): T? {
    val jsonString = getStringOrNull(key) ?: return null
    return try {
        Json.decodeFromString<T>(jsonString)
    } catch (e: Exception) {
        // Note: This is in the API module without Analytics dependency.
        // Callers should handle null return appropriately.
        // Consider using a logged version in implementation if needed.
        null
    }
}

sealed class PreferencesKey(val name: String) {
    data object ServerUrl : PreferencesKey("ServerUrl")
    data object Credentials : PreferencesKey("Credentials")
    data object ReaderSettings : PreferencesKey("ReaderSettings")
    data object DatabaseSchemaVersion : PreferencesKey("DatabaseSchemaVersion")
    data object FileLoggingEnabled : PreferencesKey("FileLoggingEnabled")
}