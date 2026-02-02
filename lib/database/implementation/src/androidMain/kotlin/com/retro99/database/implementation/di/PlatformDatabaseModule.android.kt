package com.retro99.database.implementation.di

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.retro99.database.implementation.AppDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Android implementation of platform-specific Database module.
 * Provides SQLDelight driver using Android Context.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformDatabaseModule {

    @Single
    fun providesSqlDriver(context: Context): SqlDriver {
        return AndroidSqliteDriver(
            schema = AppDatabase.Schema,
            context = context,
            name = "storyteller.db",
        )
    }
}

