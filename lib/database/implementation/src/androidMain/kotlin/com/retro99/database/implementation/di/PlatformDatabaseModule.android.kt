package com.retro99.database.implementation.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.retro99.database.api.DatabaseNameProvider
import com.retro99.database.implementation.AppDatabase
import com.retro99.database.implementation.SqlDriverFactory
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

/**
 * Android implementation of platform-specific Database module.
 * Provides SqlDriverFactory for creating per-user database drivers.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformDatabaseModule {

    @Single
    fun providesSqlDriverFactory(
        context: Context,
        @Provided preferences: Preferences,
    ): SqlDriverFactory {
        return AndroidSqlDriverFactory(context, preferences)
    }
}

private class AndroidSqlDriverFactory(
    private val context: Context,
    private val preferences: Preferences,
) : SqlDriverFactory {

    override fun createDriver(userId: String): SqlDriver {
        val databaseName = DatabaseNameProvider.buildDatabaseName(userId)
        val currentSchemaVersion = AppDatabase.Schema.version

        // Use user-scoped schema version key
        val schemaVersionKey = PreferencesKey.UserScoped(userId, "DatabaseSchemaVersion")
        val storedVersion = preferences.getLong(schemaVersionKey)

        // If schema version changed, delete old database (destructive migration)
        // Data will be re-fetched from server since this is a cache
        if (storedVersion != currentSchemaVersion) {
            context.deleteDatabase(databaseName)
            preferences.putLong(schemaVersionKey, currentSchemaVersion)
        }

        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = databaseName,
        )
    }

    override fun deleteUserDatabase(userId: String): Boolean {
        val databaseName = DatabaseNameProvider.buildDatabaseName(userId)
        return context.deleteDatabase(databaseName)
    }
}

