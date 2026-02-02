package com.retro99.database.implementation.di

import org.koin.core.annotation.Module

/**
 * Platform-specific Koin module that provides the RoomDatabase.Builder implementation.
 *
 * Uses expect/actual pattern with @Module annotation so KSP can process
 * platform-specific implementations and include them in the module graph.
 *
 * Each platform implementation provides its own database builder:
 * - Android: Uses Room.databaseBuilder with Context
 * - iOS: Uses Room.databaseBuilder with file path
 */
@Module
expect class PlatformDatabaseModule()

