package com.retro99.database.implementation.dao.books

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.retro99.database.api.books.BookEntity

/**
 * Room entity for books table.
 *
 * Only queryable fields are stored as columns.
 * Full book data is stored as JSON for easy retrieval.
 */
@Entity(tableName = "books")
data class BookRoomEntity(
    @PrimaryKey
    override val uuid: String,

    @ColumnInfo(name = "title")
    override val title: String,

    @ColumnInfo(name = "id")
    override val id: Long,

    @ColumnInfo(name = "rating")
    override val rating: Float?,

    @ColumnInfo(name = "data_json")
    override val dataJson: String,

    @ColumnInfo(name = "cached_at")
    override val cachedAt: Long,
) : BookEntity

