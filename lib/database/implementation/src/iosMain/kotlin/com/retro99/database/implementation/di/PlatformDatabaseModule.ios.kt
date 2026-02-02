package com.retro99.database.implementation.di

import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.retro99.database.implementation.AppDatabase
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

/**
 * iOS implementation of platform-specific Database module.
 * Provides RoomDatabase.Builder using iOS file system paths.
 */
@Module
actual class PlatformDatabaseModule {

    @OptIn(ExperimentalForeignApi::class)
    @Single
    fun providesDatabaseBuilder(): RoomDatabase.Builder<AppDatabase> {
        val documentDirectory = requireNotNull(
            NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )?.path
        )
        val dbFilePath = "$documentDirectory/storyteller.db"
        return Room.databaseBuilder<AppDatabase>(
            name = dbFilePath,
        ).fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
    }
}

