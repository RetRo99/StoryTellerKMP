package com.retro99.database.implementation.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.retro99.database.implementation.AppDatabase
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

/**
 * Android implementation of platform-specific Database module.
 * Provides RoomDatabase.Builder using Android Context.
 *
 * Uses BundledSQLiteDriver for consistency across platforms (Android/iOS).
 * This ensures the same SQLite version is used on all platforms.
 *
 * Context is automatically provided by Koin when using androidContext(this) in Koin initialization.
 */
@Module
actual class PlatformDatabaseModule {

    @Single
    fun providesDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
        val dbFile = context.getDatabasePath("storyteller.db")
        return Room.databaseBuilder<AppDatabase>(
            context = context,
            name = dbFile.absolutePath,
        ).fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
    }
}

