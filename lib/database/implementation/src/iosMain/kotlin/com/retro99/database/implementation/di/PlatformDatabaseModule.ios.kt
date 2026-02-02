@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package com.retro99.database.implementation.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.retro99.database.implementation.AppDatabase
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single
import platform.Foundation.NSFileManager
import platform.Foundation.NSLibraryDirectory
import platform.Foundation.NSUserDomainMask

private const val DATABASE_NAME = "storyteller.db"

/**
 * iOS implementation of platform-specific Database module.
 * Provides SQLDelight driver using iOS native driver.
 */
@Module
actual class PlatformDatabaseModule {

    @Single
    fun providesSqlDriver(@Provided preferences: Preferences): SqlDriver {
        val currentSchemaVersion = AppDatabase.Schema.version
        val storedVersion = preferences.getLong(PreferencesKey.DatabaseSchemaVersion)

        // If schema version changed, delete old database (destructive migration)
        // Data will be re-fetched from server since this is a cache
        if (storedVersion != currentSchemaVersion) {
            deleteDatabaseFile()
            preferences.putLong(PreferencesKey.DatabaseSchemaVersion, currentSchemaVersion)
        }

        return NativeSqliteDriver(
            schema = AppDatabase.Schema,
            name = DATABASE_NAME,
        )
    }

    private fun deleteDatabaseFile() {
        val fileManager = NSFileManager.defaultManager
        val libraryPaths = fileManager.URLsForDirectory(
            NSLibraryDirectory,
            NSUserDomainMask,
        )
        val libraryUrl = libraryPaths.firstOrNull() ?: return

        @Suppress("UNCHECKED_CAST")
        val libraryPath = (libraryUrl as platform.Foundation.NSURL).path ?: return
        val dbPath = "$libraryPath/$DATABASE_NAME"

        if (fileManager.fileExistsAtPath(dbPath)) {
            runCatching { fileManager.removeItemAtPath(dbPath, null) }
        }
    }
}

