package com.retro99.database.api.books

import com.retro99.database.api.DataClearable
import kotlinx.coroutines.flow.Flow

interface BookmarksDatabase : DataClearable {

    suspend fun addBookmark(bookmark: BookmarkEntity)

    fun observeBookmarks(bookUuid: String): Flow<List<BookmarkEntity>>

    suspend fun getBookmarks(bookUuid: String): List<BookmarkEntity>

    suspend fun deleteBookmark(id: String)

    suspend fun deleteBookmarksForBook(bookUuid: String)

    suspend fun updateBookmarkTitle(id: String, title: String)

    suspend fun updateBookmarkSortOrders(orders: List<Pair<String, Int>>)

    override suspend fun clearAllData()
}
