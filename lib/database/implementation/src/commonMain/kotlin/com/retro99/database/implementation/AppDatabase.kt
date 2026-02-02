package com.retro99.database.implementation

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import com.retro99.database.implementation.dao.books.BookRoomEntity

@Database(
    entities = [
        BookRoomEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@ConstructedBy(AppDatabaseConstructor::class)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase()

@Suppress("NO_ACTUAL_FOR_EXPECT")
expect object AppDatabaseConstructor : RoomDatabaseConstructor<AppDatabase> {
    override fun initialize(): AppDatabase
}