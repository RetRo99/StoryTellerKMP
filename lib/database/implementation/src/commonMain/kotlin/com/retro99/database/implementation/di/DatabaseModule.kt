package com.retro99.database.implementation.di

import androidx.room.RoomDatabase
import com.retro99.database.implementation.AppDatabase
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Configuration
import org.koin.core.annotation.Module
import org.koin.core.annotation.Single

@Module(
    includes = [
        PlatformDatabaseModule::class,
    ],
)
@Configuration
@ComponentScan("com.retro99.database.implementation")
class DatabaseModule {

    @Single
    fun provideAppDatabase(builder: RoomDatabase.Builder<AppDatabase>): AppDatabase {
        return builder.build()
    }
}
