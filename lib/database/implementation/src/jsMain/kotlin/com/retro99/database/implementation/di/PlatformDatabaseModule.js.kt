package com.retro99.database.implementation.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.worker.createDefaultWebWorkerDriver
import com.retro99.database.implementation.AppDatabase
import com.retro99.database.implementation.SqlDriverFactory
import com.retro99.preferences.api.Preferences
import com.retro99.preferences.api.PreferencesKey
import org.koin.core.annotation.Module
import org.koin.core.annotation.Provided
import org.koin.core.annotation.Single

@Module
actual class PlatformDatabaseModule {

    @Single
    fun providesSqlDriverFactory(
        @Provided preferences: Preferences,
    ): SqlDriverFactory {
        return WebSqlDriverFactory(preferences)
    }
}

private class WebSqlDriverFactory(
    private val preferences: Preferences,
) : SqlDriverFactory {

    override fun createDriver(userId: String): SqlDriver {
        val currentSchemaVersion = AppDatabase.Schema.version

        val schemaVersionKey = PreferencesKey.UserScoped(userId, "DatabaseSchemaVersion")
        val storedVersion = preferences.getLong(schemaVersionKey)

        if (storedVersion != currentSchemaVersion) {
            preferences.putLong(schemaVersionKey, currentSchemaVersion)
        }

        return createDefaultWebWorkerDriver()
    }

    override fun deleteUserDatabase(userId: String): Boolean {
        val schemaVersionKey = PreferencesKey.UserScoped(userId, "DatabaseSchemaVersion")
        preferences.remove(schemaVersionKey)
        return true
    }
}
