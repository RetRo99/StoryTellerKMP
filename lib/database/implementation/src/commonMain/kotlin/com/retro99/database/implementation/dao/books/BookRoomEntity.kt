package com.retro99.database.implementation.dao.books

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.retro99.database.api.books.BookEntity

@Entity(tableName = "books")
data class BookRoomEntity(
    @PrimaryKey
    override val uuid: String,
) : BookEntity

