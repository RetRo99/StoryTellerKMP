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
    data object ReaderSettings : PreferencesKey("ReaderSettings")
    data object DatabaseSchemaVersion : PreferencesKey("DatabaseSchemaVersion")
    data object FileLoggingEnabled : PreferencesKey("FileLoggingEnabled")
    data object CurrentlyReading : PreferencesKey("CurrentlyReading")
    data object BubblePosition : PreferencesKey("BubblePosition")
    data object OpenLastBookOnLaunch : PreferencesKey("OpenLastBookOnLaunch")
    data object RegisteredServers : PreferencesKey("RegisteredServers")
    data object ServerCredentials : PreferencesKey("ServerCredentials")
    data object ActiveServerId : PreferencesKey("ActiveServerId")
    data object SkippedLogin : PreferencesKey("SkippedLogin")

    // User profile keys (device-level, not user-scoped)
    data object UserProfiles : PreferencesKey("UserProfiles")
    data object ActiveProfileId : PreferencesKey("ActiveProfileId")
    data object ProfileMigrationVersion : PreferencesKey("ProfileMigrationVersion")

    /**
     * Dynamic key for user-scoped preferences.
     * Used to store per-user data like servers, credentials, settings.
     * Example: UserScoped("abc123", "RegisteredServers") -> "user_abc123_RegisteredServers"
     */
    data class UserScoped(val userId: String, val key: String) : PreferencesKey("user_${userId}_$key")
}