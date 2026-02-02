package com.retro99.database.implementation

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.retro99.database.api.books.BooksDatabase
import com.retro99.database.implementation.dao.books.BookRoomEntity
import com.retro99.database.implementation.dao.books.BooksDatabaseImpl
import com.retro99.database.implementation.dao.books.BooksRoomDao

@Database(
    entities = [
        BookRoomEntity::class,
    ],
    version = 2,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    protected abstract fun booksRoomDao(): BooksRoomDao

    fun booksDao(): BooksDatabase = BooksDatabaseImpl(booksRoomDao())
}

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}