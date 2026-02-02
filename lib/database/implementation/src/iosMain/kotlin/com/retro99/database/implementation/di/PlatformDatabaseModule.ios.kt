package com.retro99.database.implementation.di

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.retro99.database.implementation.AppDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * iOS implementation of platform-specific Database module.
 * Provides SQLDelight driver using iOS native driver.
 */
@Module
actual class PlatformDatabaseModule {

    @Single
    fun providesSqlDriver(): SqlDriver {
        return NativeSqliteDriver(
            schema = AppDatabase.Schema,
            name = "storyteller.db",
        )
    }
}

