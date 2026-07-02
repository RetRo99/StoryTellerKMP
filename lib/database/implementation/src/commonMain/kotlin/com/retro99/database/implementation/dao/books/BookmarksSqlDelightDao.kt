package com.retro99.database.implementation.dao.books

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.retro99.database.api.books.BookmarkEntity
import com.retro99.database.implementation.Bookmarks
import com.retro99.database.implementation.DatabaseManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

internal class BookmarksSqlDelightDao(
    private val databaseManager: DatabaseManager,
) {
    private val bookmarkQueries get() = databaseManager.getDatabase().bookmarkQueries

    suspend fun insertBookmark(bookmark: BookmarkEntity) {
        withContext(Dispatchers.Default) {
            bookmarkQueries.insertBookmark(
                id = bookmark.id,
                book_uuid = bookmark.bookUuid,
                locator_href = bookmark.locatorHref,
                locator_type = bookmark.locatorType,
                locator_title = bookmark.locatorTitle,
                progression = bookmark.progression,
                total_progression = bookmark.totalProgression,
                chapter_index = bookmark.chapterIndex?.toLong(),
                position = bookmark.position?.toLong(),
                created_at = bookmark.createdAt,
                sort_order = bookmark.sortOrder.toLong(),
            )
        }
    }

    fun observeBookmarks(bookUuid: String): Flow<List<BookmarkEntity>> {
        return bookmarkQueries.getBookmarksByBookUuid(bookUuid)
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { rows -> rows.map { it.toBookmarkEntity() } }
    }

    suspend fun getBookmarks(bookUuid: String): List<BookmarkEntity> {
        return withContext(Dispatchers.Default) {
            bookmarkQueries.getBookmarksByBookUuid(bookUuid)
                .executeAsList()
                .map { it.toBookmarkEntity() }
        }
    }

    suspend fun deleteBookmark(id: String) {
        withContext(Dispatchers.Default) {
            bookmarkQueries.deleteBookmark(id)
        }
    }

    suspend fun deleteBookmarksForBook(bookUuid: String) {
        withContext(Dispatchers.Default) {
            bookmarkQueries.deleteBookmarksByBookUuid(bookUuid)
        }
    }

    suspend fun deleteAllBookmarks() {
        withContext(Dispatchers.Default) {
            bookmarkQueries.deleteAllBookmarks()
        }
    }

    suspend fun updateBookmarkTitle(id: String, title: String) {
        withContext(Dispatchers.Default) {
            bookmarkQueries.updateBookmarkTitle(title, id)
        }
    }

    suspend fun updateBookmarkSortOrders(orders: List<Pair<String, Int>>) {
        withContext(Dispatchers.Default) {
            bookmarkQueries.transaction {
                orders.forEach { (id, sortOrder) ->
                    bookmarkQueries.updateBookmarkSortOrders(sortOrder.toLong(), id)
                }
            }
        }
    }

    private fun Bookmarks.toBookmarkEntity(): BookmarkEntity {
        return BookmarkEntityImpl(
            id = id,
            bookUuid = book_uuid,
            locatorHref = locator_href,
            locatorType = locator_type,
            locatorTitle = locator_title,
            progression = progression,
            totalProgression = total_progression,
            chapterIndex = chapter_index?.toInt(),
            position = position?.toInt(),
            createdAt = created_at,
            sortOrder = sort_order.toInt(),
        )
    }
}

private data class BookmarkEntityImpl(
    override val id: String,
    override val bookUuid: String,
    override val locatorHref: String,
    override val locatorType: String?,
    override val locatorTitle: String?,
    override val progression: Double?,
    override val totalProgression: Double?,
    override val chapterIndex: Int?,
    override val position: Int?,
    override val createdAt: String,
    override val sortOrder: Int,
) : BookmarkEntity
