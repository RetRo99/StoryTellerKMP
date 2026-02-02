package com.retro99.database.implementation.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.retro99.database.implementation.AppDatabase
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

private const val DATABASE_NAME = "storyteller.db"

/**
 * Android implementation of platform-specific Database module.
 * Provides SQLDelight driver using Android Context.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformDatabaseModule {

    @Single
    fun providesSqlDriver(
        context: Context,
        @Provided preferences: Preferences,
    ): SqlDriver {
        val currentSchemaVersion = AppDatabase.Schema.version
        val storedVersion = preferences.getLong(PreferencesKey.DatabaseSchemaVersion)

        // If schema version changed, delete old database (destructive migration)
        // Data will be re-fetched from server since this is a cache
        if (storedVersion != currentSchemaVersion) {
            context.deleteDatabase(DATABASE_NAME)
            preferences.putLong(PreferencesKey.DatabaseSchemaVersion, currentSchemaVersion)
        }

        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = DATABASE_NAME,
        )
    }
}

