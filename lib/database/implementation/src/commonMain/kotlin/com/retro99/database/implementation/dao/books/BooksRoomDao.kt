package com.retro99.database.implementation.dao.books

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface BooksRoomDao {

    @Upsert
    suspend fun upsertBooks(books: List<BookRoomEntity>)

    @Upsert
    suspend fun upsertBook(book: BookRoomEntity)

    @Query("SELECT * FROM books ORDER BY title ASC")
    fun getAllBooks(): Flow<List<BookRoomEntity>>

    @Query("SELECT * FROM books WHERE uuid = :uuid")
    suspend fun getBookByUuid(uuid: String): BookRoomEntity?

    @Query("DELETE FROM books")
    suspend fun deleteAllBooks()

    @Query("DELETE FROM books WHERE uuid = :uuid")
    suspend fun deleteBook(uuid: String)

    @Query("SELECT COUNT(*) FROM books")
    suspend fun getBooksCount(): Int

    @Query("SELECT * FROM books WHERE cached_at < :timestamp")
    suspend fun getBooksOlderThan(timestamp: Long): List<BookRoomEntity>
}

