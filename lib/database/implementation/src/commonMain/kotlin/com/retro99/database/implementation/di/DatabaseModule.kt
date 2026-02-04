package com.retro99.database.implementation.di

import app.cash.sqldelight.db.SqlDriver
import com.retro99.database.api.books.BooksDatabase
import com.retro99.database.api.books.PositionDatabase
import com.retro99.database.implementation.AppDatabase
import com.retro99.database.implementation.dao.books.BooksDatabaseImpl
import com.retro99.database.implementation.dao.books.BooksSqlDelightDao
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
    internal fun provideAppDatabase(driver: SqlDriver): AppDatabase {
        return AppDatabase(driver)
    }

    @Single
    internal fun provideBooksSqlDelightDao(database: AppDatabase): BooksSqlDelightDao {
        return BooksSqlDelightDao(database)
    }

    @Single
    internal fun provideBooksDatabase(dao: BooksSqlDelightDao): BooksDatabase {
        return BooksDatabaseImpl(dao)
    }

    @Single
    internal fun providePositionDatabase(booksDatabase: BooksDatabase): PositionDatabase {
        return booksDatabase
    }
}
